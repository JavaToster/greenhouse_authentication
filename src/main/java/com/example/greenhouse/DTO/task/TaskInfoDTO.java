package com.example.greenhouse.DTO.task;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskInfoDTO {
    private long id;
    private String name;
    private String description;
    private LocalDateTime deadline;
}
