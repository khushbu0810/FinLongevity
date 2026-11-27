package com.example.financial_engine_service.dto;

import java.math.BigDecimal;

public record CashFlowAnalysisResponse(
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpenses,
        BigDecimal monthlyDebtPayments,
        BigDecimal monthlySurplus,
        BigDecimal savingsRatePercentage,
        boolean cashFlowPositive,
        String currency
) {
}
