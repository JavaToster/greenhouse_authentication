package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.auth.AfterRegisterDataDTO;
import com.example.greenhouse.DTO.auth.AuthenticationDTO;
import com.example.greenhouse.DTO.auth.DeviceAuthRequestDTO;
import com.example.greenhouse.DTO.auth.SuccessfullyAuthenticatedDTO;
import com.example.greenhouse.services.DeviceService;
import com.example.greenhouse.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final DeviceService deviceService;

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

    @GetMapping("/device/challenge/{deviceId}")
    public ResponseEntity<String> getChallenge(@PathVariable String deviceId){
        String challenge = deviceService.generateChallenge(deviceId);
        return ResponseEntity.ok(challenge);
    }

    @PostMapping("/device/verify")
    public ResponseEntity<SuccessfullyAuthenticatedDTO> verify(@RequestBody DeviceAuthRequestDTO deviceAuthRequestDTO) throws NoSuchAlgorithmException, InvalidKeyException {
        String token = deviceService.verify(deviceAuthRequestDTO);
        return ResponseEntity.ok(new SuccessfullyAuthenticatedDTO(token));
    }
}
