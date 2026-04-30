package com.example.greenhouse.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "telemetries")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class Telemetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", referencedColumnName = "device_id")
    private Device device;

    @Column(name = "temperature", nullable = false)
    private double temperature;

    @Column(name = "air_humidity", nullable = false)
    private double airHumidity;

    @Column(name = "soil_humidity", nullable = false)
    private double soilHumidity;

    @Column(name = "illumination", nullable = false)
    private double illumination;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Telemetry(Device device, double temperature, double airHumidity, double soilHumidity, double illumination) {
        this.device = device;
        this.temperature = temperature;
        this.airHumidity = airHumidity;
        this.soilHumidity = soilHumidity;
        this.illumination = illumination;
        this.createdAt = Instant.now();
    }
}
