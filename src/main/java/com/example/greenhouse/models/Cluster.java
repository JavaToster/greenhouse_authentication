package com.example.greenhouse.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "clusters")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "telegram_id")
    private User owner;

    @OneToMany(mappedBy = "cluster")
    List<Device> devices;

    @ManyToMany
    @JoinTable(
            name = "clusters_workers",
            joinColumns = @JoinColumn(name = "cluster_id"),
            inverseJoinColumns = @JoinColumn(name = "worker_id")
    )
    private Set<User> workers = new HashSet<>();

    @OneToMany(mappedBy = "cluster")
    private List<Task> tasks;

    public void addWorker(User worker){
        this.workers.add(worker);
        worker.getClustersToWork().add(this);
    }

    public void removeWorker(User worker){
        this.workers.remove(worker);
        worker.getClusters().remove(this);
    }
}
