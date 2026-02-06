package com.example.greenhouse.DAO.cluster;

import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.postgres.ClusterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClusterDAO {
    private final ClusterRepository clusterRepository;

    public long count(){
        return clusterRepository.count();
    }

    public Cluster save(Cluster cluster){
        return clusterRepository.save(cluster);
    }

    public List<Cluster> findAll(){
        return clusterRepository.findAll();
    }

    public List<Cluster> find(User owner){
        return clusterRepository.findByOwner(owner);
    }

    public Cluster find(UUID id){
        return clusterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Кластера с таким id не существует"));
    }
}
