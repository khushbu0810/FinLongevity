package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read-only representation of the existing Investment Service
 * GET /api/investments response.
 *
 * Enum-like fields are strings intentionally: the Financial Engine is a
 * consumer of the Investment Service contract and should not duplicate its
 * persistence enums.
 */
public record InvestmentServiceInvestmentResponse(
        UUID id,
        Long identityUserId,
        UUID providerProductId,
        String productNameSnapshot,
        String productType,
        BigDecimal investedAmount,
        String currency,
        BigDecimal units,
        String status,
        UUID goalId,
        Instant openedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
