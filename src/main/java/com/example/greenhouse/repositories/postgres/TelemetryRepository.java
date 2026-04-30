package com.example.greenhouse.repositories.postgres;

import com.example.greenhouse.models.Telemetry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    Page<Telemetry> findByDeviceClusterId(UUID clusterId, Pageable pageable);
}
