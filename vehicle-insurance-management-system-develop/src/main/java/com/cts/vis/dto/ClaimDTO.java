package com.cts.vis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

public class ClaimDTO {

    @Data
    public static class FileRequest {

        @NotNull(message = "Policy is required")
        private Long policyId;

        @NotNull(message = "Claim amount is required")
        @Positive(message = "Claim amount must be greater than zero")
        private BigDecimal claimAmount;

        @NotBlank(message = "Claim reason is required")
        @Size(min = 5, message = "Claim reason must be at least 5 characters")
        private String claimReason;
    }
}