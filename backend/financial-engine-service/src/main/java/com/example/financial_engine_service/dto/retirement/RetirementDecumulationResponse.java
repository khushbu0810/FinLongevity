package com.example.financial_engine_service.dto.retirement;

import java.math.BigDecimal;
import java.util.List;

/** Summary of the post-retirement drawdown projection. */
public record RetirementDecumulationResponse(
        Integer retirementAge,
        Integer planningAge,
        BigDecimal startingRetirementCorpus,
        BigDecimal totalFundedWithdrawals,
        BigDecimal totalUnfundedShortfall,
        Integer corpusExhaustionAge,
        BigDecimal projectedCorpusAtPlanningAge,
        boolean sustainableThroughPlanningAge,
        List<RetirementDecumulationPointResponse> yearlyProjection
) {
    public RetirementDecumulationResponse {
        yearlyProjection = yearlyProjection == null ? List.of() : List.copyOf(yearlyProjection);
    }
}
