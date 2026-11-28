package com.example.financial_engine_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record CustomerDataMappingSummary(
        String currency,
        BigDecimal normalizedMonthlyIncome,
        BigDecimal normalizedMonthlyExpenses,
        BigDecimal monthlyDebtPayments,
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal liquidAssets,
        int includedIncomeSources,
        int includedExpenseItems,
        int includedAssets,
        int includedLiabilities,
        List<String> warnings
) {
}
