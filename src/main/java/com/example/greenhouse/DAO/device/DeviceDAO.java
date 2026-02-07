package com.example.greenhouse.DAO.device;

import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.repositories.postgres.DeviceRepository;
import com.example.greenhouse.util.enums.DeviceStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceDAO {
    private final DeviceRepository deviceRepository;

    public long count(){
        return deviceRepository.count();
    }

    public long count(DeviceStatus deviceStatus){
        return deviceRepository.countByStatus(deviceStatus);
    }

    public List<Device> saveAll(List<Device> devices){
        return deviceRepository.saveAll(devices);
    }

    public void updateStatusByClusterId(UUID clusterId, DeviceStatus status){
        deviceRepository.updateStatusByClusterId(clusterId, status);
    }

    public Device findById(UUID id){
        return deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Девайса с таким id не существует"));
    }

    public void remove(UUID id){
        deviceRepository.deleteById(id);
    }
}
