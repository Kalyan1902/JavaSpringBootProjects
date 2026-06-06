package com.cts.vis.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyDTO {

    @Data
    public static class CreateRequest {

        @NotNull(message = "Vehicle is required")
        private Long vehicleId;

        @NotNull(message = "Coverage amount is required")
        @Positive(message = "Coverage amount must be greater than zero")
        private BigDecimal coverageAmount;

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or future")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        @Future(message = "End date must be a future date")
        private LocalDate endDate;   // ✅ added
    }

    @Data
    public static class UpdateRequest {

        @NotNull(message = "Coverage amount is required")
        @Positive(message = "Coverage amount must be greater than zero")
        private BigDecimal coverageAmount;
    }
}