package com.example.greenhouse.repositories.postgres;

import com.example.greenhouse.models.Cluster;
import com.example.greenhouse.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCluster(Cluster cluster);
}
