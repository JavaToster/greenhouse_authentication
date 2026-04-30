package com.example.greenhouse.DTO.device;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DeviceTelemetryDTO {
    private UUID deviceId;
    private List<TelemetryDTO> telemetries;
}
