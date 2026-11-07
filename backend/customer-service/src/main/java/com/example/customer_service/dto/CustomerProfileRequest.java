package com.example.customer_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerProfileRequest(
        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @Size(max = 20)
        String phone,

        @Past
        LocalDate dateOfBirth,

        @Size(max = 30)
        String gender
) {
}
