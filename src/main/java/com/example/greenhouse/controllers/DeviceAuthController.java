package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.auth.DeviceAuthRequestDTO;
import com.example.greenhouse.DTO.auth.SuccessfullyAuthenticatedDTO;
import com.example.greenhouse.DTO.device.CreateDeviceDTO;
import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import com.example.greenhouse.services.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceAuthController {
    private final DeviceService deviceService;

    @GetMapping("/challenge/{deviceId}")
    public ResponseEntity<String> getChallenge(@PathVariable String deviceId){
        String challenge = deviceService.generateChallenge(deviceId);
        return ResponseEntity.ok(challenge);
    }

    @PostMapping("/verify")
    public ResponseEntity<SuccessfullyAuthenticatedDTO> verify(@RequestBody DeviceAuthRequestDTO deviceAuthRequestDTO){
        String token = deviceService.verify(deviceAuthRequestDTO);
        return ResponseEntity.ok(new SuccessfullyAuthenticatedDTO(token));
    }

    @PostMapping("/create")
    public ResponseEntity<?> addNewDevice(@Valid @RequestBody CreateDeviceDTO createDeviceDTO){
        CreatedDeviceDTO createdDeviceDTO = deviceService.addNewDevice(createDeviceDTO);
        return ResponseEntity.ok(createdDeviceDTO);
    }
}
