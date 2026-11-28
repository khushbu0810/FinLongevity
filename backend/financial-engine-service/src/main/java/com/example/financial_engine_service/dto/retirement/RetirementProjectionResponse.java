package com.example.financial_engine_service.dto.retirement;

import java.math.BigDecimal;
import java.util.List;

public record RetirementProjectionResponse(
        Integer currentAge,
        Integer retirementAge,
        Integer yearsProjected,
        BigDecimal startingCorpus,
        BigDecimal totalFutureContributions,
        BigDecimal projectedCorpus,
        BigDecimal projectedMonthlyRetirementExpenses,
        BigDecimal projectedAnnualRetirementExpenses,
        BigDecimal estimatedCorpusRequiredAtRetirement,
        boolean projectedCorpusMeetsTarget,
        String currency,
        List<RetirementProjectionPointResponse> yearlyProjection,
        BigDecimal projectedMonthlyRetirementIncome,
        BigDecimal projectedMonthlyIncomeGap,
        BigDecimal projectedMonthlyIncomeSurplus,
        List<RetirementIncomeStreamProjectionResponse> retirementIncomeStreams,
        List<RetirementExpenseCategoryProjectionResponse> retirementExpenseBreakdown,
        RetirementDecumulationResponse retirementDecumulation,
        RetirementCorpusRequirementResponse corpusRequirement
) {
    public RetirementProjectionResponse {
        yearlyProjection = yearlyProjection == null ? List.of() : List.copyOf(yearlyProjection);
        retirementIncomeStreams = retirementIncomeStreams == null ? List.of() : List.copyOf(retirementIncomeStreams);
        retirementExpenseBreakdown = retirementExpenseBreakdown == null ? List.of() : List.copyOf(retirementExpenseBreakdown);
    }

    public BigDecimal projectedGrossMonthlyRetirementIncome() { return projectedMonthlyRetirementIncome; }
    public BigDecimal projectedGrossAnnualRetirementIncome() { return projectedMonthlyRetirementIncome.multiply(BigDecimal.valueOf(12)); }


    /** Compatibility constructor retaining the Phase 1 Step 4 canonical response shape. */
    public RetirementProjectionResponse(Integer currentAge, Integer retirementAge, Integer yearsProjected,
            BigDecimal startingCorpus, BigDecimal totalFutureContributions, BigDecimal projectedCorpus,
            BigDecimal projectedMonthlyRetirementExpenses, BigDecimal projectedAnnualRetirementExpenses,
            BigDecimal estimatedCorpusRequiredAtRetirement, boolean projectedCorpusMeetsTarget, String currency,
            List<RetirementProjectionPointResponse> yearlyProjection,
            BigDecimal projectedMonthlyRetirementIncome, BigDecimal projectedMonthlyIncomeGap,
            BigDecimal projectedMonthlyIncomeSurplus,
            List<RetirementIncomeStreamProjectionResponse> retirementIncomeStreams,
            List<RetirementExpenseCategoryProjectionResponse> retirementExpenseBreakdown,
            RetirementDecumulationResponse retirementDecumulation) {
        this(currentAge, retirementAge, yearsProjected, startingCorpus, totalFutureContributions, projectedCorpus,
                projectedMonthlyRetirementExpenses, projectedAnnualRetirementExpenses,
                estimatedCorpusRequiredAtRetirement, projectedCorpusMeetsTarget, currency, yearlyProjection,
                projectedMonthlyRetirementIncome, projectedMonthlyIncomeGap, projectedMonthlyIncomeSurplus,
                retirementIncomeStreams, retirementExpenseBreakdown, retirementDecumulation, null);
    }

    public RetirementProjectionResponse(Integer currentAge, Integer retirementAge, Integer yearsProjected,
            BigDecimal startingCorpus, BigDecimal totalFutureContributions, BigDecimal projectedCorpus,
            BigDecimal projectedMonthlyRetirementExpenses, BigDecimal projectedAnnualRetirementExpenses,
            BigDecimal estimatedCorpusRequiredAtRetirement, boolean projectedCorpusMeetsTarget, String currency,
            List<RetirementProjectionPointResponse> yearlyProjection,
            BigDecimal projectedMonthlyRetirementIncome, BigDecimal projectedMonthlyIncomeGap,
            BigDecimal projectedMonthlyIncomeSurplus,
            List<RetirementIncomeStreamProjectionResponse> retirementIncomeStreams,
            List<RetirementExpenseCategoryProjectionResponse> retirementExpenseBreakdown) {
        this(currentAge, retirementAge, yearsProjected, startingCorpus, totalFutureContributions, projectedCorpus,
                projectedMonthlyRetirementExpenses, projectedAnnualRetirementExpenses,
                estimatedCorpusRequiredAtRetirement, projectedCorpusMeetsTarget, currency, yearlyProjection,
                projectedMonthlyRetirementIncome, projectedMonthlyIncomeGap, projectedMonthlyIncomeSurplus,
                retirementIncomeStreams, retirementExpenseBreakdown, null, null);
    }

    public RetirementProjectionResponse(Integer currentAge, Integer retirementAge, Integer yearsProjected,
            BigDecimal startingCorpus, BigDecimal totalFutureContributions, BigDecimal projectedCorpus,
            BigDecimal projectedMonthlyRetirementExpenses, BigDecimal projectedAnnualRetirementExpenses,
            BigDecimal estimatedCorpusRequiredAtRetirement, boolean projectedCorpusMeetsTarget, String currency,
            List<RetirementProjectionPointResponse> yearlyProjection,
            BigDecimal projectedMonthlyRetirementIncome, BigDecimal projectedMonthlyIncomeGap,
            BigDecimal projectedMonthlyIncomeSurplus,
            List<RetirementIncomeStreamProjectionResponse> retirementIncomeStreams) {
        this(currentAge, retirementAge, yearsProjected, startingCorpus, totalFutureContributions, projectedCorpus,
                projectedMonthlyRetirementExpenses, projectedAnnualRetirementExpenses,
                estimatedCorpusRequiredAtRetirement, projectedCorpusMeetsTarget, currency, yearlyProjection,
                projectedMonthlyRetirementIncome, projectedMonthlyIncomeGap, projectedMonthlyIncomeSurplus,
                retirementIncomeStreams, List.of(), null, null);
    }

    public RetirementProjectionResponse(Integer currentAge, Integer retirementAge, Integer yearsProjected,
            BigDecimal startingCorpus, BigDecimal totalFutureContributions, BigDecimal projectedCorpus,
            BigDecimal projectedMonthlyRetirementExpenses, BigDecimal projectedAnnualRetirementExpenses,
            BigDecimal estimatedCorpusRequiredAtRetirement, boolean projectedCorpusMeetsTarget, String currency,
            List<RetirementProjectionPointResponse> yearlyProjection) {
        this(currentAge, retirementAge, yearsProjected, startingCorpus, totalFutureContributions, projectedCorpus,
                projectedMonthlyRetirementExpenses, projectedAnnualRetirementExpenses,
                estimatedCorpusRequiredAtRetirement, projectedCorpusMeetsTarget, currency, yearlyProjection,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of(), null, null);
    }
}
