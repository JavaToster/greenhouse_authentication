package com.example.greenhouse.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Registration data for a new user")
public record SingUpDTO(
        @Schema(description = "Telegram user identifier", example = "123456789")
        @NotNull(message = "Telegram ID is required")
        Long telegramId,
        @Schema(description = "User email address", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @Schema(description = "User password", example = "secret-password")
        @NotBlank(message = "Password is required")
        String password
) {}
