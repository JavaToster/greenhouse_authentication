package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.device.CreateDeviceDTO;
import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import com.example.greenhouse.services.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreatedDeviceDTO> addNewDevice(@Valid @RequestBody CreateDeviceDTO createDeviceDTO){
        CreatedDeviceDTO createdDeviceDTO = deviceService.addNewDevice(createDeviceDTO);
        return ResponseEntity.ok(createdDeviceDTO);
    }
}
