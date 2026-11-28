package com.example.financial_engine_service.dto.retirement;

import java.math.BigDecimal;

/** One year of retirement cash flow and corpus movement. */
public record RetirementDecumulationPointResponse(
        Integer year,
        Integer age,
        BigDecimal openingCorpus,
        BigDecimal annualExpenses,
        BigDecimal annualRecurringIncome,
        BigDecimal requiredWithdrawal,
        BigDecimal fundedWithdrawal,
        BigDecimal unfundedShortfall,
        BigDecimal annualIncomeSurplus,
        BigDecimal investmentGrowth,
        BigDecimal closingCorpus,
        boolean corpusExhausted
) {
}
