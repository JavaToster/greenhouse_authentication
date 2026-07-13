package com.example.greenhouse.DTO.user;

import com.example.greenhouse.util.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User profile information")
public record UserInfoDTO(
        @Schema(description = "Telegram user identifier", example = "123456789")
        long telegramId,
        @Schema(description = "User email address", example = "user@example.com")
        String email,
        @Schema(description = "Assigned user role", example = "ROLE_OWNER")
        Role role
) {}
