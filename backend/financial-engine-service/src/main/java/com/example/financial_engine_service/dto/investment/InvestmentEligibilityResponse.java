package com.example.financial_engine_service.dto.investment;

import java.util.List;

/** Customer-level investment eligibility results, preserving each investment currency. */
public record InvestmentEligibilityResponse(
        int totalInvestments,
        int eligibleInvestments,
        List<InvestmentEligibilityAssessment> investments
) {
    public InvestmentEligibilityResponse {
        investments = investments == null ? List.of() : List.copyOf(investments);
    }
}
