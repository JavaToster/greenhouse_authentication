package com.example.greenhouse.services;

import com.example.greenhouse.DTO.cluster.ClusterTelemetryDTO;
import com.example.greenhouse.DTO.device.AddTelemetryDTO;
import com.example.greenhouse.DTO.device.DeviceTelemetryDTO;
import com.example.greenhouse.DTO.device.TelemetryDTO;
import com.example.greenhouse.models.Telemetry;
import com.example.greenhouse.store.TelemetryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.greenhouse.store.DeviceStore;
import com.example.greenhouse.models.Device;
import com.example.greenhouse.store.ClusterStore;
import com.example.greenhouse.models.Cluster;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.greenhouse.util.enums.DeviceStatus;
import org.springframework.security.authentication.BadCredentialsException;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TelemetryService {
    private final TelemetryStore telemetryStore;
    private final DeviceStore deviceStore;
    private final ClusterStore clusterStore;
    @Transactional
    public Telemetry add(AddTelemetryDTO dto, UUID deviceId) {
        log.debug("Adding telemetry: temperature={}, airHumidity={}, soilHumidity={}, illumination={}",
                dto.getTemperature(), dto.getAirHumidity(), dto.getSoilHumidity(), dto.getIllumination());


        Device device = deviceStore.findById(deviceId);
        if (device.getStatus() != DeviceStatus.ACTIVE) {
            throw new BadCredentialsException("Device is not active");
        }
        Telemetry telemetry = new Telemetry(
                device,
                dto.getTemperature(),
                dto.getAirHumidity(),
                dto.getSoilHumidity(),
                dto.getIllumination()
        );

        return telemetryStore.save(telemetry);
    }

    public Telemetry findById(Long id) {
        return telemetryStore.findById(id);
    }

    public ClusterTelemetryDTO findByCluster(long ownerId, UUID clusterId, int page, int size) {
        Cluster cluster = clusterStore.findById(clusterId);
        if (cluster.getOwner().getTelegramId() != ownerId) {
            throw new AccessDeniedException("User is not the owner of this cluster");
        }

        List<Device> devices = deviceStore.findByClusterId(clusterId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Telemetry> telemetryPage = telemetryStore.findByClusterId(clusterId, pageable);
        List<Telemetry> telemetries = telemetryPage.getContent();

        Map<UUID, List<TelemetryDTO>> telemetryByDevice = new HashMap<>();
        for (Telemetry telemetry : telemetries) {
            UUID deviceId = telemetry.getDevice().getId();
            telemetryByDevice
                    .computeIfAbsent(deviceId, k -> new ArrayList<>())
                    .add(toDto(telemetry));
        }

        List<DeviceTelemetryDTO> deviceTelemetry = devices.stream()
                .map(device -> {
                    DeviceTelemetryDTO dto = new DeviceTelemetryDTO();
                    dto.setDeviceId(device.getId());
                    dto.setTelemetries(telemetryByDevice.getOrDefault(device.getId(), List.of()));
                    return dto;
                })
                .toList();

        ClusterTelemetryDTO result = new ClusterTelemetryDTO();
        result.setClusterId(clusterId);
        result.setDevices(deviceTelemetry);
        result.setPage(telemetryPage.getNumber());
        result.setSize(telemetryPage.getSize());
        result.setTotalElements(telemetryPage.getTotalElements());
        result.setTotalPages(telemetryPage.getTotalPages());
        return result;
    }

    private TelemetryDTO toDto(Telemetry telemetry) {
        TelemetryDTO dto = new TelemetryDTO();
        dto.setId(telemetry.getId());
        dto.setDeviceId(telemetry.getDevice().getId());
        dto.setTemperature(telemetry.getTemperature());
        dto.setAirHumidity(telemetry.getAirHumidity());
        dto.setSoilHumidity(telemetry.getSoilHumidity());
        dto.setIllumination(telemetry.getIllumination());
        dto.setCreatedAt(telemetry.getCreatedAt());
        return dto;
    }
}
