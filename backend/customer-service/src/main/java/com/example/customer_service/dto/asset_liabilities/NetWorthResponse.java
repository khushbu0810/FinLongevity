package com.example.customer_service.dto.asset_liabilities;

import java.math.BigDecimal;
import java.time.Instant;

public record NetWorthResponse(
        String currency,
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal netWorth,
        Instant calculatedAt
) {
}
