package com.example.financial_engine_service.dto;

import com.example.financial_engine_service.enums.*;

import java.math.BigDecimal;
import java.util.UUID;

public record FinancialProjectionResponse(
        UUID id,
        UUID analysisId,
        ScenarioType scenarioType,
        ProjectionPeriodType periodType,
        Integer periodNumber,
        BigDecimal projectedIncome,
        BigDecimal projectedExpenses,
        BigDecimal projectedContributions,
        BigDecimal projectedCorpus,
        BigDecimal projectedNetWorth
) {
}
