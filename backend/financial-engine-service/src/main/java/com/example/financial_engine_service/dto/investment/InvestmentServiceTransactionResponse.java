package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read-only representation of the existing Investment Service
 * GET /api/investments/{investmentId}/transactions response.
 */
public record InvestmentServiceTransactionResponse(
        UUID id,
        UUID investmentId,
        String transactionType,
        String status,
        BigDecimal amount,
        String currency,
        BigDecimal units,
        String paymentReference,
        String description,
        Instant processedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
