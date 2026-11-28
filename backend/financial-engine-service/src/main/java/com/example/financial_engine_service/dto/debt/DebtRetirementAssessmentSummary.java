package com.example.financial_engine_service.dto.debt;

import java.math.BigDecimal;

/** Currency-specific totals. Estimated retirement balance covers only assessed liabilities. */
public record DebtRetirementAssessmentSummary(
        String currency,
        BigDecimal totalOutstandingDebt,
        BigDecimal totalMonthlyEmi,
        BigDecimal estimatedOutstandingAtRetirementForAssessedLiabilities,
        Integer totalLiabilityCount,
        Integer assessedLiabilityCount,
        Integer continuingIntoRetirementCount,
        Integer paidOffBeforeRetirementCount,
        Integer nonAmortizingCount,
        Integer insufficientDataCount
) {
}
