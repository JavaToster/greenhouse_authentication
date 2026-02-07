package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.services.ClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final ClusterService clusterService;

    @GetMapping("/clusters")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<ClusterInfoDTO>> getWorkerClusters(Principal principal){
        return ResponseEntity.ok(clusterService.findByWorker(Long.parseLong(principal.getName())));
    }
}
