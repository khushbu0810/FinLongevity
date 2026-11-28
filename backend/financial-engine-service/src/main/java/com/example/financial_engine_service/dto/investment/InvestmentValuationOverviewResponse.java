package com.example.financial_engine_service.dto.investment;

import java.util.List;

/** Customer-scoped portfolio valuation overview returned by Investment Service. */
public record InvestmentValuationOverviewResponse(
        List<InvestmentValuationOverviewItemResponse> investments
) {
    public InvestmentValuationOverviewResponse {
        investments = investments == null ? List.of() : List.copyOf(investments);
    }

    public static InvestmentValuationOverviewResponse empty() {
        return new InvestmentValuationOverviewResponse(List.of());
    }
}
