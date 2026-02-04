package com.example.greenhouse.DTO.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemStatsForAdminDTO {
    private long totalClusters;
    private long totalDevices;
    private long totalActiveDevices;
    private long totalUsers;
    private long totalOwners;
    private long totalInstallers;
    private long totalWorkers;
    private long totalUnknownUsers;
}
