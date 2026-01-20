package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.auth.AfterRegisterDataDTO;
import com.example.greenhouse.DTO.auth.AuthenticationDTO;
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

    @PostMapping("/sing-up")
    public ResponseEntity<AfterRegisterDataDTO> singUp(@Valid @RequestBody AuthenticationDTO authenticationDTO){
        AfterRegisterDataDTO afterRegisterDataDTO = userService.singUp(authenticationDTO);

        return ResponseEntity.ok(afterRegisterDataDTO);
    }

    @PostMapping("/sing-in")
    public ResponseEntity<AfterRegisterDataDTO> singIn(@Valid @RequestBody AuthenticationDTO authenticationDTO){
        AfterRegisterDataDTO afterRegisterDataDTO = userService.singIn(authenticationDTO);

        return ResponseEntity.ok(afterRegisterDataDTO);
    }
}
