package com.example.greenhouse.services;

import com.example.greenhouse.DTO.admin.SystemStatsForAdminDTO;
import com.example.greenhouse.repositories.postgres.ClusterRepository;
import com.example.greenhouse.repositories.postgres.DeviceRepository;
import com.example.greenhouse.repositories.postgres.UserRepository;
import com.example.greenhouse.util.enums.DeviceStatus;
import com.example.greenhouse.util.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final UserRepository userRepository;
    private final ClusterRepository clusterRepository;
    private final DeviceRepository deviceRepository;

    @Cacheable(value = "systemStats", key = "'global_admin_stats'")
    public SystemStatsForAdminDTO getSystemStats(){
        return SystemStatsForAdminDTO.builder()
                .totalClusters(clusterRepository.count())
                .totalDevices(deviceRepository.count())
                .totalActiveDevices(deviceRepository.countByStatus(DeviceStatus.ACTIVE))
                .totalUsers(userRepository.count())
                .totalOwners(userRepository.countByRole(Role.ROLE_OWNER))
                .totalInstallers(userRepository.countByRole(Role.ROLE_INSTALLER))
                .totalWorkers(userRepository.countByRole(Role.ROLE_WORKER))
                .totalUnknownUsers(userRepository.countByRole(Role.ROLE_UNKNOWN))
                .build();
    }
}
