package com.example.customer_service.dto.expenses;

import com.example.customer_service.model.expenses.ExpenseCategory;
import com.example.customer_service.model.expenses.ExpenseFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String name,
        ExpenseCategory category,
        BigDecimal amount,
        String currency,
        ExpenseFrequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        boolean essential,
        boolean active,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
