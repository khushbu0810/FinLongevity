package com.example.financial_engine_service.dto.retirement;

import java.math.BigDecimal;
import java.util.List;

public record RetirementProjectionPointResponse(
        Integer year,
        Integer age,
        BigDecimal projectedCorpus,
        BigDecimal projectedMonthlyExpenses,
        BigDecimal projectedAnnualExpenses,
        BigDecimal projectedContributions,
        BigDecimal projectedMonthlyRetirementIncome,
        BigDecimal projectedMonthlyIncomeGap,
        BigDecimal projectedMonthlyIncomeSurplus,
        List<RetirementExpenseCategoryProjectionResponse> expenseBreakdown
) {
    public RetirementProjectionPointResponse {
        expenseBreakdown = expenseBreakdown == null ? List.of() : List.copyOf(expenseBreakdown);
    }

    public RetirementProjectionPointResponse(Integer year, Integer age,
            BigDecimal projectedCorpus, BigDecimal projectedMonthlyExpenses,
            BigDecimal projectedAnnualExpenses, BigDecimal projectedContributions,
            BigDecimal projectedMonthlyRetirementIncome,
            BigDecimal projectedMonthlyIncomeGap, BigDecimal projectedMonthlyIncomeSurplus) {
        this(year, age, projectedCorpus, projectedMonthlyExpenses, projectedAnnualExpenses,
                projectedContributions, projectedMonthlyRetirementIncome,
                projectedMonthlyIncomeGap, projectedMonthlyIncomeSurplus, List.of());
    }

    public RetirementProjectionPointResponse(Integer year, Integer age,
            BigDecimal projectedCorpus, BigDecimal projectedMonthlyExpenses,
            BigDecimal projectedAnnualExpenses, BigDecimal projectedContributions) {
        this(year, age, projectedCorpus, projectedMonthlyExpenses, projectedAnnualExpenses,
                projectedContributions, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
    }
}
