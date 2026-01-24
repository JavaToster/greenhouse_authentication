package com.example.greenhouse.models.device;

import com.example.greenhouse.models.user.User;
import com.example.greenhouse.util.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "secret", nullable = false)
    private String secret;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "telegram_id")
    private User owner;
}
