package com.example.greenhouse.DTO.cluster;

import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import lombok.Data;

import java.util.List;

@Data
public class RegisteredClusterDTO {
    private long ownerId;
    private List<CreatedDeviceDTO> devices;
}
