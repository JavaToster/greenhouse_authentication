package com.example.greenhouse.repositories.postgres;

import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.util.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Device d
    set d.status = :status
    where d.cluster.id = :clusterId
""")
    int updateStatusByClusterId(UUID clusterId, DeviceStatus status);

}
