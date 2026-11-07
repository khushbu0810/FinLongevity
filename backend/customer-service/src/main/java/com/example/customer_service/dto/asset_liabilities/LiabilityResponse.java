package com.example.customer_service.dto.asset_liabilities;

import com.example.customer_service.model.asset_liabilities.LiabilityCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LiabilityResponse(
        UUID id,
        String name,
        LiabilityCategory category,
        BigDecimal outstandingAmount,
        String currency,
        BigDecimal annualInterestRate,
        BigDecimal monthlyEmi,
        LocalDate nextPaymentDate,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
