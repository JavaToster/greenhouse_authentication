package com.example.greenhouse.store;

import com.example.greenhouse.models.Telemetry;
import com.example.greenhouse.repositories.postgres.TelemetryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Component
@RequiredArgsConstructor
public class TelemetryStore implements GenericStore<Telemetry, Long> {
    private final TelemetryRepository telemetryRepository;

    @Override
    public Telemetry findById(Long id) {
        return telemetryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Telemetry with id " + id + " not found"));
    }

    @Override
    public Telemetry save(Telemetry telemetry) {
        return telemetryRepository.save(telemetry);
    }

    public Page<Telemetry> findByClusterId(UUID clusterId, Pageable pageable) {
        return telemetryRepository.findByDeviceClusterId(clusterId, pageable);
    }
}
