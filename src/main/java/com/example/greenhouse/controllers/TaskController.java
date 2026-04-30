package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.task.CreateNewTaskDTO;
import com.example.greenhouse.DTO.task.TaskInfoDTO;
import com.example.greenhouse.DTO.task.WorkerTasksDTO;
import com.example.greenhouse.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/api/owner/tasks/new")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<TaskInfoDTO> createNewTask(
            @Valid @RequestBody CreateNewTaskDTO createNewTaskDTO,
            Principal principal
    ){
        TaskInfoDTO taskInfoDTO = taskService.create(Long.parseLong(principal.getName()), createNewTaskDTO);
        return ResponseEntity.ok(taskInfoDTO);
    }

    @GetMapping("/api/owner/clusters/{clusterId}/tasks")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TaskInfoDTO>> getTasksOfCluster(
            Principal principal,
            @PathVariable("clusterId") UUID clusterId
    ){
        List<TaskInfoDTO> tasks = taskService.findByCluster(Long.parseLong(principal.getName()), clusterId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/api/worker/tasks")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<WorkerTasksDTO>> getTasksOfWorker(Principal principal){
        return ResponseEntity.ok(taskService.getTasksOfWorker(Long.parseLong(principal.getName())));
    }

    @PostMapping("/api/worker/tasks/{taskId}/complete")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<?> completeTask(@PathVariable("taskId") long id, Principal principal) throws BadRequestException {
        taskService.complete(id, Long.parseLong(principal.getName()));
        return ResponseEntity.ok().build();
    }
}
