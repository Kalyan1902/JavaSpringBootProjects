package com.cts.vis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String address;

    @Column(nullable = false)
    private LocalDate createdDate;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) createdDate = LocalDate.now();
    }

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}