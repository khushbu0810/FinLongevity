package com.example.customer_service.dto.household;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HouseholdRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter code")
        String currency
) {
}
