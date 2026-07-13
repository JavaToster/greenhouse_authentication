package com.example.greenhouse.DTO.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Role assignment request for a user")
public record AssignRoleToPersonDTO(
        @Schema(description = "Target role", example = "ROLE_ADMIN")
        @NotBlank(message = "Role is required")
        @Pattern(
                regexp = "^ROLE_(ADMIN|OWNER|WORKER|INSTALLER|UNKNOWN)$",
                message = "Invalid role. Available roles: ROLE_ADMIN, ROLE_OWNER, ROLE_WORKER, ROLE_INSTALLER, ROLE_UNKNOWN"
        )
        String role
) {}
