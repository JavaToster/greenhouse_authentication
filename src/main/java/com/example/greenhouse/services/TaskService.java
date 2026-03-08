package com.example.greenhouse.services;

import com.example.greenhouse.store.ClusterStore;
import com.example.greenhouse.store.TaskStore;
import com.example.greenhouse.store.UserStore;
import com.example.greenhouse.DTO.task.CreateNewTaskDTO;
import com.example.greenhouse.DTO.task.TaskInfoDTO;
import com.example.greenhouse.DTO.task.WorkerTasksDTO;
import com.example.greenhouse.models.Cluster;
import com.example.greenhouse.models.Task;
import com.example.greenhouse.models.User;
import com.example.greenhouse.util.Convertor;
import com.example.greenhouse.util.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskStore taskStore;
    private final ClusterStore clusterStore;
    private final Convertor convertor;
    private final UserStore userStore;

    @Transactional
    public TaskInfoDTO create(long ownerId, CreateNewTaskDTO createNewTaskDTO) {
        log.info("Attempting to create new task for {} cluster", createNewTaskDTO.getClusterId());
        Cluster cluster = clusterStore.findById(createNewTaskDTO.getClusterId());

        if (cluster.getOwner().getTelegramId() != ownerId){
            throw new AccessDeniedException("You don't create new task, because you aren't owner");
        }

        Task task = new Task(createNewTaskDTO.getName(), createNewTaskDTO.getDescription(),
                cluster, createNewTaskDTO.getDeadline());

        taskStore.save(task);
        log.info("Task successfully created with id {}", task.getId());
        return convertor.convertToTaskInfoDTO(task);
    }

    public List<TaskInfoDTO> findByCluster(long ownerId, UUID clusterId) {
        Cluster cluster = clusterStore.findById(clusterId);

        if (cluster.getOwner().getTelegramId() != ownerId){
            throw new AccessDeniedException("You don't create new task, because you aren't owner");
        }

        return taskStore.findByCluster(cluster)
                .stream().map(convertor::convertToTaskInfoDTO)
                .toList();
    }

    public List<WorkerTasksDTO> getTasksOfWorker(long workerId) {
        User worker = userStore.findById(workerId);

        Set<Cluster> clustersOfWorker = worker.getClustersToWork();

        List<WorkerTasksDTO> workerTasks = new ArrayList<>();

        clustersOfWorker.forEach(cluster -> workerTasks.add(new WorkerTasksDTO(convertor.convertToClusterInfoDTO(cluster), convertor.convertToTaskInfoDTO(cluster.getTasks()))));

        return workerTasks;
    }

    @Transactional
    public void complete(long taskId, long workerId) throws BadRequestException {
        Task task = taskStore.findById(taskId);

        if (!userStore.isWorkerInCluster(workerId, task.getCluster().getId())){
            throw new AccessDeniedException("You aren't worker of this cluster!");
        }

        if(task.getStatus() == TaskStatus.COMPLETED){
            throw new BadRequestException("This task is already completed!");
        }
        task.setStatus(TaskStatus.COMPLETED);
    }
}
