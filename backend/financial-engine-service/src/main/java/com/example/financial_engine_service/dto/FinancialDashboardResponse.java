package com.example.financial_engine_service.dto;

import java.util.List;

public record FinancialDashboardResponse(
        FinancialAnalysisResponse latestAnalysis,
        List<FinancialProjectionResponse> projections
) {
    public FinancialDashboardResponse {
        projections = projections == null ? List.of() : List.copyOf(projections);
    }
}
