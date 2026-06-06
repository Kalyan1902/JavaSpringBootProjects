package com.cts.vis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    private int yearOfManufacture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private LocalDate createdDate;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) createdDate = LocalDate.now();
    }
}