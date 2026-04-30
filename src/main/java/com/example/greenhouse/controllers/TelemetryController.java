package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.cluster.ClusterTelemetryDTO;
import com.example.greenhouse.DTO.device.AddTelemetryDTO;
import com.example.greenhouse.security.token.DevicePrincipal;
import com.example.greenhouse.services.TelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TelemetryController {
    private final TelemetryService telemetryService;

    @PostMapping("/api/devices/telemetry/add")
    @PreAuthorize("hasRole('DEVICE')")
    public ResponseEntity<Void> add(@AuthenticationPrincipal DevicePrincipal principal, @Valid @RequestBody AddTelemetryDTO dto) {
        telemetryService.add(dto, principal.deviceId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/owner/clusters/{clusterId}/telemetries")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ClusterTelemetryDTO> getClusterTelemetries(
            Principal principal,
            @PathVariable("clusterId") UUID clusterId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size
    ){
        ClusterTelemetryDTO telemetry = telemetryService.findByCluster(Long.parseLong(principal.getName()), clusterId, page, size);
        return ResponseEntity.ok(telemetry);
    }
}
