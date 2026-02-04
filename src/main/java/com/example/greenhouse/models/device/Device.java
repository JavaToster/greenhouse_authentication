package com.example.greenhouse.models.device;

import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.util.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Data
@Entity
@Table(name = "devices")
public class Device implements Persistable<UUID> {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "secret", nullable = false)
    private String secret;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;

    @ManyToOne
    @JoinColumn(name = "cluster_id", referencedColumnName = "id")
    private Cluster cluster;

    @Transient
    private String rawSecret;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew(){
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew(){
        this.isNew = false;
    }
}
