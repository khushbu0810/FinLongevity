package com.example.financial_engine_service.dto.retirement;

import com.example.financial_engine_service.enums.ScenarioType;

import java.math.BigDecimal;

public record ScenarioSimulationResponse(
        ScenarioType scenarioType,
        BigDecimal annualReturnPercentage,
        BigDecimal annualInflationPercentage,
        BigDecimal projectedCorpus,
        BigDecimal projectedMonthlyExpenses,
        BigDecimal estimatedRealCorpus,
        Integer years
) {
}
