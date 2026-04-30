package com.example.greenhouse.models;

import com.example.greenhouse.util.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Persistable;

import java.util.UUID;
import java.util.List;

@Data
@Entity
@Table(name = "devices")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Device implements Persistable<UUID> {
    @Id
    @Column(name = "device_id", nullable = false)
    @EqualsAndHashCode.Include
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

    @OneToMany(mappedBy = "device")
    private List<Telemetry> telemetries;

    @PostPersist
    @PostLoad
    void markNotNew(){
        this.isNew = false;
    }
}
