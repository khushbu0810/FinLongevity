package com.example.financial_engine_service.dto.retirement;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ScenarioSimulationRequest(
        @NotNull
        @DecimalMin("0.00")
        BigDecimal currentCorpus,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal monthlyContribution,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal currentMonthlyExpenses,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal annualReturnPercentage,

        @NotNull @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal annualInflationPercentage,

        @NotNull
        @Positive
        Integer years
) {
}
