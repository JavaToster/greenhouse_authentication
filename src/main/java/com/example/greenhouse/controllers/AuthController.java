package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.auth.*;
import com.example.greenhouse.services.DeviceService;
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
    private final DeviceService deviceService;

    @PostMapping("/sing-up")
    public ResponseEntity<AfterRegisterDataDTO> singUp(@Valid @RequestBody SingUpDTO authenticationDTO){
        AfterRegisterDataDTO afterRegisterDataDTO = userService.singUp(authenticationDTO);

        return ResponseEntity.ok(afterRegisterDataDTO);
    }

    @PostMapping("/sing-in")
    public ResponseEntity<AfterRegisterDataDTO> singIn(@Valid @RequestBody SingInDTO authenticationDTO){
        AfterRegisterDataDTO afterRegisterDataDTO = userService.singIn(authenticationDTO);

        return ResponseEntity.ok(afterRegisterDataDTO);
    }

    @PostMapping("/device/challenge/{deviceId}")
    public ResponseEntity<String> getChallenge(@PathVariable String deviceId){
        String challenge = deviceService.generateChallenge(deviceId);
        return ResponseEntity.ok(challenge);
    }

    @PostMapping("/device/verify")
    public ResponseEntity<SuccessfullyAuthenticatedDTO> verify(@RequestBody DeviceAuthRequestDTO deviceAuthRequestDTO) {
        String token = deviceService.verify(deviceAuthRequestDTO);
        return ResponseEntity.ok(new SuccessfullyAuthenticatedDTO(token));
    }




}
