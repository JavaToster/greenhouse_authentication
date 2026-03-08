package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.cluster.WorkerAssigmentDTO;
import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.task.CreateNewTaskDTO;
import com.example.greenhouse.DTO.task.TaskInfoDTO;
import com.example.greenhouse.services.ClusterService;
import com.example.greenhouse.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {
    private final ClusterService clusterService;
    private final TaskService taskService;

    @GetMapping("/clusters")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ClusterInfoDTO>> getClusters(Principal principal){
        return ResponseEntity.ok(clusterService.findByOwnerId(Long.parseLong(principal.getName())));
    }

    @PostMapping("/clusters/{clusterId}/workers/add")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> addWorkerToCluster(Principal principal, @PathVariable("clusterId") UUID clusterId, @Valid @RequestBody WorkerAssigmentDTO dto) throws BadRequestException, AccessDeniedException {
        clusterService.addWorkerToCluster(Long.parseLong(principal.getName()), clusterId, dto.getWorkerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clusters/{clusterId}/workers/remove")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> removeWorkerFromCluster(Principal principal, @PathVariable("clusterId") UUID clusterId, @Valid @RequestBody WorkerAssigmentDTO dto) throws AccessDeniedException, BadRequestException {
        clusterService.removeWorkerFromCluster(Long.parseLong(principal.getName()), clusterId, dto.getWorkerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tasks/new")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<TaskInfoDTO> createNewTask(@Valid @RequestBody CreateNewTaskDTO createNewTaskDTO, Principal principal){
        TaskInfoDTO taskInfoDTO = taskService.create(Long.parseLong(principal.getName()), createNewTaskDTO);
        return ResponseEntity.ok(taskInfoDTO);
    }

    @GetMapping("/clusters/{clusterId}/tasks")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TaskInfoDTO>> getTasksOfCluster(Principal principal, @PathVariable("clusterId") UUID clusterId){
        List<TaskInfoDTO> tasks = taskService.findByCluster(Long.parseLong(principal.getName()), clusterId);
        return ResponseEntity.ok(tasks);
    }
}
