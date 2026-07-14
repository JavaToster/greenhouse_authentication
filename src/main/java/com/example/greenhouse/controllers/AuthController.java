package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.auth.*;
import com.example.greenhouse.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user sign-up and sign-in")
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Sign up user"
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User signed up successfully")
    })
    public ResponseEntity<SuccessfullyAuthenticatedDTO> singUp(@Valid @RequestBody SingUpDTO authenticationDTO){
        log.debug("Received sign-up HTTP request for telegramId={}", authenticationDTO.telegramId());
        SuccessfullyAuthenticatedDTO successfullyAuthenticatedDTO = userService.singUp(authenticationDTO);

        return ResponseEntity.ok(successfullyAuthenticatedDTO);
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Sign in user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User signed in successfully")
    })
    @SecurityRequirements
    public ResponseEntity<SuccessfullyAuthenticatedDTO> singIn(@Valid @RequestBody SingInDTO authenticationDTO){
        log.debug("Received sign-in HTTP request for telegramId={}", authenticationDTO.telegramId());
        SuccessfullyAuthenticatedDTO successfullyAuthenticatedDTO = userService.singIn(authenticationDTO);

        return ResponseEntity.ok(successfullyAuthenticatedDTO);
    }
}