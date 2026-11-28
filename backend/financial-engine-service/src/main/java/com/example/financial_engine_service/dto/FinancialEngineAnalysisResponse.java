package com.example.financial_engine_service.dto;

public record FinancialEngineAnalysisResponse(
        FinancialAnalysisResponse analysis,
        FinancialSnapshotAnalysisResponse financialSnapshot,
        LongevityScoreResponse longevityScore
) {
}
