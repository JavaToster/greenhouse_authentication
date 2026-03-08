package com.example.greenhouse.store;

import com.example.greenhouse.models.Cluster;
import com.example.greenhouse.models.Task;
import com.example.greenhouse.repositories.postgres.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskStore implements GenericStore<Task, Long> {
    private final TaskRepository taskRepository;

    @Override
    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task with " + id + " id not found"));
    }

    @Override
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> findByCluster(Cluster cluster){
        return taskRepository.findByCluster(cluster);
    }
}
