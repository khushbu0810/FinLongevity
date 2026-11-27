package com.example.insurance_service.dto;

import com.example.insurance_service.model.InsuranceApplicationStatus;
import com.example.insurance_service.model.InsuranceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InsuranceApplicationResponse(
        UUID id,
        UUID providerProductId,
        String productNameSnapshot,
        InsuranceType insuranceType,
        String applicantName,
        LocalDate applicantDateOfBirth,
        BigDecimal coverageAmount,
        String currency,
        InsuranceApplicationStatus status,
        Instant submittedAt,
        Instant decisionAt,
        String decisionNote,
        Instant createdAt,
        Instant updatedAt
) {}
