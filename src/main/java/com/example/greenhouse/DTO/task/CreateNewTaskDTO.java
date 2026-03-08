package com.example.greenhouse.DTO.task;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateNewTaskDTO {
    @NotBlank(message = "name should be not empty")
    @Size(min = 5, message = "the size of name should be more 5 characters")
    private String name;

    @NotBlank(message = "description should be not empty")
    @Size(min = 10, message = "the size of description should be more 10 characters")
    private String description;

    @NotNull(message = "enter cluster id")
    private UUID clusterId;

    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;
}
