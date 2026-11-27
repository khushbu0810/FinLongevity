
package com.example.goal_service.dto;

import java.math.BigDecimal;

public record GroupMemberContributionAnalyticsResponse(
        Long userId,
        BigDecimal totalContributionAmount,
        BigDecimal confirmedAmount,
        BigDecimal pendingAmount,
        long successfulContributionCount,
        long failedContributionCount,
        BigDecimal contributionPercentage

) {
}
