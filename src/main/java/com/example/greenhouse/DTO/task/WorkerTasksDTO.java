package com.example.greenhouse.DTO.task;

import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WorkerTasksDTO {
    private ClusterInfoDTO cluster;
    private List<TaskInfoDTO> tasks;
}
