package com.cts.vis.dto;

import com.cts.vis.model.VehicleType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Year;

public class VehicleDTO {

    /* ================= CREATE VEHICLE ================= */

    @Data
    public static class CreateRequest {

        @NotBlank(message = "Registration number is required")
        @Pattern(
                regexp = "^[A-Z0-9]{5,12}$",
                message = "Registration number must contain only uppercase letters and numbers"
        )
        private String registrationNumber;

        @NotBlank(message = "Make is required")
        @Pattern(
                regexp = "^[A-Za-z ]+$",
                message = "Make must contain only letters"
        )
        private String make;

        @NotBlank(message = "Model is required")
        @Pattern(
                regexp = "^[A-Za-z0-9 ]+$",
                message = "Model can contain letters and numbers"
        )
        private String model;

        @NotNull(message = "Year of manufacture is required")
        @Min(value = 1980, message = "Year must be after 1980")
        @Max(value = Year.MAX_VALUE, message = "Invalid year")
        private Integer yearOfManufacture;

        @NotNull(message = "Vehicle type is required")
        private VehicleType vehicleType;
    }

    /* ================= UPDATE VEHICLE ================= */

    @Data
    public static class UpdateRequest {

        @NotBlank(message = "Registration number is required")
        @Pattern(
                regexp = "^[A-Z0-9]{5,12}$",
                message = "Registration number must contain only uppercase letters and numbers"
        )
        private String registrationNumber;

        @NotBlank(message = "Make is required")
        @Pattern(regexp = "^[A-Za-z ]+$", message = "Make must contain only letters")
        private String make;

        @NotBlank(message = "Model is required")
        @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Model can contain letters and numbers")
        private String model;

        @NotNull(message = "Year of manufacture is required")
        @Min(value = 1980, message = "Year must be after 1980")
        @Max(value = Year.MAX_VALUE, message = "Invalid year")
        private Integer yearOfManufacture;

        @NotNull(message = "Vehicle type is required")
        private VehicleType vehicleType;
    }
}