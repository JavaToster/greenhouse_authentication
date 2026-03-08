package com.example.greenhouse.models;

import com.example.greenhouse.util.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tasks")
@EqualsAndHashCode
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public Task(String name, String description,
                Cluster cluster, LocalDateTime deadline){
        this.name = name;
        this.description = description;
        this.cluster = cluster;
        this.deadline = deadline;
        this.status = TaskStatus.NOT_COMPLETED;
    }
}
