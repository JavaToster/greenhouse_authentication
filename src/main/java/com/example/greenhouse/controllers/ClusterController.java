package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.cluster.RegisterNewClusterDTO;
import com.example.greenhouse.DTO.cluster.WorkerAssigmentDTO;
import com.example.greenhouse.DTO.device.ClusterDevicesTempSecretsDTO;
import com.example.greenhouse.DTO.device.DevicesTempSecretDTO;
import com.example.greenhouse.services.ClusterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClusterController {
    private final ClusterService clusterService;

    @GetMapping("/api/owner/clusters")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ClusterInfoDTO>> getOwnerClusters(Principal principal){
        return ResponseEntity.ok(clusterService.findByOwnerId(Long.parseLong(principal.getName())));
    }

    @GetMapping("/api/worker/clusters")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<ClusterInfoDTO>> getWorkerClusters(Principal principal){
        return ResponseEntity.ok(clusterService.findByWorker(Long.parseLong(principal.getName())));
    }

    @GetMapping("/api/admin/clusters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClusterInfoDTO>> getAllClusters(){
        return ResponseEntity.ok(clusterService.findAllClusters());
    }

    @PostMapping("/api/owner/clusters/{clusterId}/workers/add")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> addWorkerToCluster(
            Principal principal,
            @PathVariable("clusterId") UUID clusterId,
            @Valid @RequestBody WorkerAssigmentDTO dto
    ) throws BadRequestException, AccessDeniedException {
        clusterService.addWorkerToCluster(Long.parseLong(principal.getName()), clusterId, dto.getWorkerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/owner/clusters/{clusterId}/workers/remove")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> removeWorkerFromCluster(
            Principal principal,
            @PathVariable("clusterId") UUID clusterId,
            @Valid @RequestBody WorkerAssigmentDTO dto
    ) throws AccessDeniedException, BadRequestException {
        clusterService.removeWorkerFromCluster(Long.parseLong(principal.getName()), clusterId, dto.getWorkerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/installer/clusters/new")
    @PreAuthorize("hasAnyRole('INSTALLER', 'ADMIN')")
    public ResponseEntity<DevicesTempSecretDTO> registerNewCluster(@Valid @RequestBody RegisterNewClusterDTO registerNewClusterDTO){
        DevicesTempSecretDTO devicesTempSecretDTO = clusterService.registerNewCluster(registerNewClusterDTO);
        return ResponseEntity.ok(devicesTempSecretDTO);
    }

    @GetMapping("/api/installer/devices/secrets/{token}")
    @PreAuthorize("hasAnyRole('INSTALLER', 'ADMIN')")
    public ResponseEntity<List<ClusterDevicesTempSecretsDTO>> getSecrets(@PathVariable("token") UUID token){
        List<ClusterDevicesTempSecretsDTO> secrets = clusterService.getRawKeysAndActivate(token);
        return ResponseEntity.ok(secrets);
    }
}
