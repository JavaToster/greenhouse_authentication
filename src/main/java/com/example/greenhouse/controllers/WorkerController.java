package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.task.TaskInfoDTO;
import com.example.greenhouse.DTO.task.WorkerTasksDTO;
import com.example.greenhouse.services.ClusterService;
import com.example.greenhouse.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final ClusterService clusterService;
    private final TaskService taskService;

    @GetMapping("/clusters")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<ClusterInfoDTO>> getWorkerClusters(Principal principal){
        return ResponseEntity.ok(clusterService.findByWorker(Long.parseLong(principal.getName())));
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<WorkerTasksDTO>> getTasksOfCluster(Principal principal){
        return ResponseEntity.ok(taskService.getTasksOfWorker(Long.parseLong(principal.getName())));
    }

    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<?> completeTask(@PathVariable("taskId") long id, Principal principal) throws BadRequestException {
        taskService.complete(id, Long.parseLong(principal.getName()));
        return ResponseEntity.ok().build();
    }
}
