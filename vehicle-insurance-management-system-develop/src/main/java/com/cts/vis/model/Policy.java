package com.cts.vis.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Policy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false, unique = true)
    private String policyNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal premiumAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus policyStatus;
}