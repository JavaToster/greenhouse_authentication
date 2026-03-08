package com.example.greenhouse.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SingUpDTO {
    @NotNull(message = "Telegram Id не может быть пустым")
    private long telegramId;
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}
