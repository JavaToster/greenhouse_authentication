package com.example.greenhouse.models.user;

import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.util.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "telegram_id")
    private long telegramId;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Cluster> clusters ;

}
