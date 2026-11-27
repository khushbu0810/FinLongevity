package com.example.goal_service.dto;

import com.example.goal_service.model.GoalContributionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GoalContributionResponse(
        UUID id,
        UUID goalId,
        Long contributorUserId,
        BigDecimal amount,
        String currency,
        GoalContributionStatus status,
        String paymentReference,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
