package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.admin.SystemStatsForAdminDTO;
import com.example.greenhouse.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SystemController {
    private final AdminService adminService;

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemStatsForAdminDTO> systemStats(){
        return ResponseEntity.ok(adminService.getSystemStats());
    }
}
