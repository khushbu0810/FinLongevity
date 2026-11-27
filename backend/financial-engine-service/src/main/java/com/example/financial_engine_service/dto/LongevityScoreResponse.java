package com.example.financial_engine_service.dto;

import com.example.financial_engine_service.enums.FinancialHealthBand;

import java.math.BigDecimal;

public record LongevityScoreResponse(
        Integer score,
        FinancialHealthBand healthBand,
        BigDecimal savingsComponent,
        BigDecimal emergencyFundComponent,
        BigDecimal debtBurdenComponent,
        BigDecimal netWorthComponent,
        String explanation
) {
}
