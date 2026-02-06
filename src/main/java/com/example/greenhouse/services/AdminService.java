package com.example.greenhouse.services;

import com.example.greenhouse.DAO.device.DeviceDAO;
import com.example.greenhouse.DAO.cluster.ClusterDAO;
import com.example.greenhouse.DAO.user.UserDAO;
import com.example.greenhouse.DTO.admin.SystemStatsForAdminDTO;
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
    private final UserDAO userDAO;
    private final ClusterDAO clusterDAO;
    private final DeviceDAO deviceDAO;

    @Cacheable(value = "systemStats", key = "'global_admin_stats'")
    public SystemStatsForAdminDTO getSystemStats(){
        return SystemStatsForAdminDTO.builder()
                .totalClusters(clusterDAO.count())
                .totalDevices(deviceDAO.count())
                .totalActiveDevices(deviceDAO.count(DeviceStatus.ACTIVE))
                .totalUsers(userDAO.count())
                .totalOwners(userDAO.count(Role.ROLE_OWNER))
                .totalInstallers(userDAO.count(Role.ROLE_INSTALLER))
                .totalWorkers(userDAO.count(Role.ROLE_WORKER))
                .totalUnknownUsers(userDAO.count(Role.ROLE_UNKNOWN))
                .build();
    }
}
