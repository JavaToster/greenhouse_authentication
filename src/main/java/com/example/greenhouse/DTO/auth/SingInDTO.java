package com.example.greenhouse.DTO.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SingInDTO {
    @NotNull(message = "Telegram Id не может быть пустым")
    private long telegramId;
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}
