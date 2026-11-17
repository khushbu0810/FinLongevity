package com.example.investment_service.dto.transaction;

import com.example.investment_service.model.transaction.InvestmentTransactionStatus;
import com.example.investment_service.model.transaction.InvestmentTransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvestmentTransactionResponse(
        UUID id,
        UUID investmentId,
        InvestmentTransactionType transactionType,
        InvestmentTransactionStatus status,
        BigDecimal amount,
        String currency,
        BigDecimal units,
        String paymentReference,
        String description,
        Instant processedAt,
        Instant createdAt,
        Instant updatedAt
) {}
