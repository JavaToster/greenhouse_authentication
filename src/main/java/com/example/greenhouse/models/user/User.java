package com.example.greenhouse.models.user;

import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.util.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Говорим использовать только помеченные поля
public class User {
    @Id
    @Column(name = "telegram_id")
    @EqualsAndHashCode.Include
    private long telegramId;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Cluster> clusters ;

    @ManyToMany(mappedBy = "workers")
    private Set<Cluster> clustersToWork = new HashSet<>();
}
