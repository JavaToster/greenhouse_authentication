package com.example.greenhouse.DTO.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Batch request with Telegram IDs")
public record UserInfoBatchRequestDTO(
        @Schema(description = "Telegram user identifiers", example = "[123456789, 987654321]")
        @NotEmpty(message = "User IDs set must not be empty")
        @Size(max = 500, message = "You cannot request more than 500 users in one batch")
        Set<Long> userIds
) {}
