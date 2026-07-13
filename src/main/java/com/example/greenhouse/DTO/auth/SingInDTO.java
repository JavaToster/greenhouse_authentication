package com.example.greenhouse.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Credentials for user sign-in")
public record SingInDTO(
        @Schema(description = "Telegram user identifier", example = "123456789")
        @NotNull(message = "Telegram ID is required")
        Long telegramId,
        @Schema(description = "User password", example = "secret-password")
        @NotBlank(message = "Password is required")
        String password
) {}
