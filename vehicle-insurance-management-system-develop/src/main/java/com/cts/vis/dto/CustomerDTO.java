package com.cts.vis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class CustomerDTO {

    @Data
    public static class RegisterRequest {

        @NotBlank(message = "Name is required")
        @Pattern(
                regexp = "^[A-Za-z ]+$",
                message = "Name must contain only letters and spaces"
        )
        private String name;

        @NotBlank(message = "Email is required")
        @Pattern(
                regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$",
                message = "Email must be valid and end with .com"
        )
        private String email;

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Phone number must be exactly 10 digits"
        )
        private String phone;

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be atleast 8 characters with letters, numbers and special characters"
        )
        private String password;
    }

    // ✅ PROFILE UPDATE (lighter validation)
    @Data
    public static class ProfileUpdateRequest {

        @NotBlank(message = "Name is required")
        @Pattern(
                regexp = "^[A-Za-z ]+$",
                message = "Name must contain only letters and spaces"
        )
        private String name;

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Phone number must be exactly 10 digits"
        )
        private String phone;

        @NotBlank(message = "Address is required")
        private String address;
    }
}