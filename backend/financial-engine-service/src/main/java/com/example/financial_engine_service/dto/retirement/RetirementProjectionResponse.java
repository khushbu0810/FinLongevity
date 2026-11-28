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
        List<RetirementProjectionPointResponse> yearlyProjection
) {
}
