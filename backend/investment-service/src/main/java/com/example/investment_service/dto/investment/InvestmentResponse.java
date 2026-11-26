package com.example.investment_service.dto.investment;

import com.example.investment_service.model.investment.InvestmentProductType;
import com.example.investment_service.model.investment.InvestmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvestmentResponse(
        UUID id,
        Long identityUserId,
        UUID providerProductId,
        String productNameSnapshot,
        InvestmentProductType productType,
        BigDecimal investedAmount,
        String currency,
        BigDecimal units,
        InvestmentStatus status,
        UUID goalId,
        Instant openedAt,
        Instant createdAt,
        Instant updatedAt
) {}
