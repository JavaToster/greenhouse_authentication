package com.example.greenhouse.DTO.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatedDeviceDTO {
    private String uuid;
    private String secret;
    private long ownerTelegramId;
}
