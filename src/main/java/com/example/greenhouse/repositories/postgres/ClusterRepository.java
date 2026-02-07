package com.example.greenhouse.repositories.postgres;

import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, UUID> {
    List<Cluster> findByOwner(User owner);

    List<Cluster> findByWorkersTelegramId(long workerId);
}
