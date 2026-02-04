package com.example.greenhouse.DTO.cluster;

import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import com.example.greenhouse.models.device.Device;
import lombok.Data;

import java.util.List;

@Data
public class RegisteredClusterDTO {
    private long ownerId;
    private List<CreatedDeviceDTO> devices;
}
