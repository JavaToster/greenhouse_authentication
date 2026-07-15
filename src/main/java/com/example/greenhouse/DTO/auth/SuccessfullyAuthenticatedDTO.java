package com.example.greenhouse.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication result containing the issued JWT")
public record SuccessfullyAuthenticatedDTO(
        @Schema(
                description = "JWT access token issued for the authenticated device",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJyb2xlIjoiREVWSUNFIn0.dQw4w9WgXcQ"
        )
        String jwt
) {}
