package com.example.goal_service.dto;

import com.example.goal_service.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialGoalResponse(
        UUID id,
        String name,
        String description,
        GoalType goalType,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        BigDecimal progressPercentage,
        String currency,
        LocalDate targetDate,
        GoalStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
