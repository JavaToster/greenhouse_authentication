package com.example.greenhouse.services;

import com.example.greenhouse.store.DeviceStore;
import com.example.greenhouse.store.ClusterStore;
import com.example.greenhouse.store.UserStore;
import com.example.greenhouse.DTO.admin.SystemStatsForAdminDTO;
import com.example.greenhouse.util.enums.DeviceStatus;
import com.example.greenhouse.util.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final UserStore userStore;
    private final ClusterStore clusterStore;
    private final DeviceStore deviceStore;

    @Cacheable(value = "systemStats", key = "'global_admin_stats'")
    public SystemStatsForAdminDTO getSystemStats(){
        log.info("Generating new system stats for Admin");
        return SystemStatsForAdminDTO.builder()
                .totalClusters(clusterStore.count())
                .totalDevices(deviceStore.count())
                .totalActiveDevices(deviceStore.count(DeviceStatus.ACTIVE))
                .totalUsers(userStore.count())
                .totalOwners(userStore.count(Role.ROLE_OWNER))
                .totalInstallers(userStore.count(Role.ROLE_INSTALLER))
                .totalWorkers(userStore.count(Role.ROLE_WORKER))
                .totalUnknownUsers(userStore.count(Role.ROLE_UNKNOWN))
                .build();
    }
}
