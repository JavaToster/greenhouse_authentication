package com.example.greenhouse.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication result containing the issued JWT")
public record SuccessfullyAuthenticatedDTO(String jwt) {}
