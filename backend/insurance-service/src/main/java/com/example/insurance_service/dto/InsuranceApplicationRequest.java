package com.example.insurance_service.dto;

import com.example.insurance_service.model.InsuranceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InsuranceApplicationRequest(
        @NotNull
        UUID providerProductId,

        @NotBlank
        @Size(max = 180)
        String productNameSnapshot,

        @NotNull
        InsuranceType insuranceType,

        @NotBlank
        @Size(max = 150)
        String applicantName,

        @NotNull
        @Past
        LocalDate applicantDateOfBirth,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal coverageAmount,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency
) {
}
