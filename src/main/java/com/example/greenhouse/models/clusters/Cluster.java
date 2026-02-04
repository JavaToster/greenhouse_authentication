package com.example.greenhouse.models.clusters;

import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.models.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "clusters")
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
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
}
