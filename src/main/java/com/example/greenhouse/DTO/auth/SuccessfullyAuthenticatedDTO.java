package com.example.greenhouse.DTO.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuccessfullyAuthenticatedDTO {
    private String jwt;
}
