package com.example.customer_service.dto.income;

import com.example.customer_service.model.income.IncomeFrequency;
import com.example.customer_service.model.income.IncomeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record IncomeSourceResponse(
        UUID id,
        IncomeType incomeType,
        String sourceName,
        BigDecimal amount,
        IncomeFrequency frequency,
        String currency,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
