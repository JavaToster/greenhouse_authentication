package com.example.greenhouse.controllers;

import com.example.greenhouse.services.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @DeleteMapping("/api/admin/devices/{id}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UUID> removeDevice(@PathVariable("id") UUID deviceId){
        return ResponseEntity.ok(deviceService.remove(deviceId));
    }
}
