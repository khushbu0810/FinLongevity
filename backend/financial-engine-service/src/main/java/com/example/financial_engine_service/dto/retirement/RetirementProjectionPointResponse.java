package com.example.financial_engine_service.dto.retirement;

import java.math.BigDecimal;

public record RetirementProjectionPointResponse(
        Integer year,
        Integer age,
        BigDecimal projectedCorpus,
        BigDecimal projectedMonthlyExpenses,
        BigDecimal projectedAnnualExpenses,
        BigDecimal projectedContributions
) {
}
