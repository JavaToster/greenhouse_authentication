package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.auth.*;
import com.example.greenhouse.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<SuccessfullyAuthenticatedDTO> singUp(@Valid @RequestBody SingUpDTO authenticationDTO){
        SuccessfullyAuthenticatedDTO successfullyAuthenticatedDTO = userService.singUp(authenticationDTO);

        return ResponseEntity.ok(successfullyAuthenticatedDTO);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SuccessfullyAuthenticatedDTO> singIn(@Valid @RequestBody SingInDTO authenticationDTO){
        SuccessfullyAuthenticatedDTO successfullyAuthenticatedDTO = userService.singIn(authenticationDTO);

        return ResponseEntity.ok(successfullyAuthenticatedDTO);
    }
}
