package com.example.greenhouse.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationDTO {
    @NotNull(message = "Telegram Id не может быть пустым")
    private long telegramId;
    @Email(message = "Некорректный формат email")
    private String email;
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}
