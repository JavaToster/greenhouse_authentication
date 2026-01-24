package com.example.greenhouse.DTO.device;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateDeviceDTO {
    @Min(value=1, message = "Неправильный айди")
    private long telegramId;
}
