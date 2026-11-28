package com.example.investment_service.dto.valuation;

import java.util.List;

public record InvestmentValuationOverviewResponse(
        List<InvestmentValuationOverviewItemResponse> investments
) {
    public InvestmentValuationOverviewResponse {
        investments = investments == null ? List.of() : List.copyOf(investments);
    }
}
