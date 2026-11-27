
package com.example.goal_service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GroupGoalAnalyticsResponse(
        UUID goalId,
        String goalName,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        BigDecimal progressPercentage,
        String currency,
        long totalContributions,
        long confirmedContributions,
        long pendingContributions,
        long failedContributions,
        List<GroupMemberContributionAnalyticsResponse> memberAnalytics

) {
}
