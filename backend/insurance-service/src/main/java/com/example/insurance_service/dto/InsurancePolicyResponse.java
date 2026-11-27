package com.example.insurance_service.dto;

import com.example.insurance_service.model.InsurancePolicyStatus;
import com.example.insurance_service.model.PremiumFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InsurancePolicyResponse(
        UUID id,
        UUID applicationId,
        String policyNumber,
        UUID providerProductId,
        BigDecimal coverageAmount,
        BigDecimal premiumAmount,
        String currency,
        PremiumFrequency premiumFrequency,
        InsurancePolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextPremiumDueDate,
        Instant createdAt,
        Instant updatedAt
) {}
