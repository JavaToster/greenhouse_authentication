package com.example.greenhouse.DTO.device;

import com.example.greenhouse.util.enums.DeviceStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class DeviceInfoDTO {
    private UUID id;
    private DeviceStatus deviceStatus;
}
