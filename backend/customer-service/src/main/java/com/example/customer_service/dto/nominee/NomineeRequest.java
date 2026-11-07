package com.example.customer_service.dto.nominee;

import com.example.customer_service.model.nominee.NomineeRelationship;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NomineeRequest(

        @NotBlank(message = "Nominee full name is required")
        @Size(max = 120, message = "Full name cannot exceed 120 characters")
        String fullName,

        @NotNull(message = "Relationship is required")
        NomineeRelationship relationship,

        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @Email(message = "Enter a valid email address")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @Pattern(
                regexp = "^[0-9+()\\- ]{7,20}$",
                message = "Phone number format is invalid"
        )
        String phone,

        @NotNull(message = "Allocation percentage is required")
        @DecimalMin(value = "0.01", message = "Allocation must be at least 0.01%")
        @DecimalMax(value = "100.00", message = "Allocation cannot exceed 100%")
        BigDecimal allocationPercentage,

        @Size(max = 120, message = "Guardian name cannot exceed 120 characters")
        String guardianName,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {
}
