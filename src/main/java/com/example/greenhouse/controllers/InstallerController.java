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

    @PutMapping("/clusters/new")
    @PreAuthorize("hasAnyRole('INSTALLER', 'ADMIN')")
    public ResponseEntity<DevicesTempSecretDTO> registerNewCluster(@Valid @RequestBody RegisterNewClusterDTO registerNewClusterDTO){
        DevicesTempSecretDTO devicesTempSecretDTO = clusterService.registerNewCluster(registerNewClusterDTO);
        return ResponseEntity.ok(devicesTempSecretDTO);
    }

    @GetMapping("/devices/secrets/{cluster_id}")
    @PreAuthorize("hasAnyRole('INSTALLER', 'ADMIN')")
    public ResponseEntity<List<ClusterDevicesTempSecretsDTO>> getSecrets(@PathVariable("cluster_id") UUID clusterId){
        List<ClusterDevicesTempSecretsDTO> secrets = clusterService.getRawKeysAndActivate(clusterId);
        return ResponseEntity.ok(secrets);
    }
}
