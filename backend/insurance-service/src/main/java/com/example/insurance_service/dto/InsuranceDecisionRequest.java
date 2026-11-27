package com.example.insurance_service.dto;

import com.example.insurance_service.model.PremiumFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InsuranceDecisionRequest(
        @NotNull
        Decision decision,

        @Size(max = 1000)
        String decisionNote,

        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal premiumAmount,

        PremiumFrequency premiumFrequency,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextPremiumDueDate
) {
    public enum Decision {
        APPROVE,
        REJECT
    }
}
