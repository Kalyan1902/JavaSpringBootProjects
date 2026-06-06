package com.cts.vis.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "claims")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal claimAmount;

    @Column(nullable = false, length = 1000)
    private String claimReason;

    @Column(nullable = false)
    private LocalDate claimDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus claimStatus;
}
