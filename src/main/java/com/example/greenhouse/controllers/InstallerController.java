package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.cluster.RegisterNewClusterDTO;
import com.example.greenhouse.DTO.cluster.RegisteredClusterDTO;
import com.example.greenhouse.DTO.device.ClusterDevicesTempSecretsDTO;
import com.example.greenhouse.DTO.device.DevicesTempSecretDTO;
import com.example.greenhouse.services.ClusterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/installer")
@RequiredArgsConstructor
public class InstallerController {

    private final ClusterService clusterService;

    @PostMapping("/clusters/new")
    @PreAuthorize("hasAnyRole('INSTALLER', 'ADMIN')")
    public ResponseEntity<DevicesTempSecretDTO> registerNewCluster(@Valid @RequestBody RegisterNewClusterDTO registerNewClusterDTO){
        DevicesTempSecretDTO devicesTempSecretDTO = clusterService.registerNewCluster(registerNewClusterDTO);
        return ResponseEntity.ok(devicesTempSecretDTO);
    }

    @GetMapping("/devices/secrets/{token}")
    @PreAuthorize("hasAnyRole('INSTALLER', 'ADMIN')")
    public ResponseEntity<List<ClusterDevicesTempSecretsDTO>> getSecrets(@PathVariable("token") UUID token){
        List<ClusterDevicesTempSecretsDTO> secrets = clusterService.getRawKeysAndActivate(token);
        return ResponseEntity.ok(secrets);
    }
}
