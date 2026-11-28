package com.example.financial_engine_service.dto;

import java.math.BigDecimal;

public record NetWorthAnalysisResponse(
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal netWorth,
        boolean netWorthPositive,
        String currency
) {
}
