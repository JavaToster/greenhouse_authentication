package com.example.greenhouse.DTO.cluster;

import com.example.greenhouse.DTO.device.DeviceTelemetryDTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ClusterTelemetryDTO {
    private UUID clusterId;
    private List<DeviceTelemetryDTO> devices;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
