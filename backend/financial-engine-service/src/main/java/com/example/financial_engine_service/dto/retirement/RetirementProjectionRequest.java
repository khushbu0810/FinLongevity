package com.example.financial_engine_service.dto.retirement;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RetirementProjectionRequest(
        @NotNull
        @DecimalMin("0.00")
        BigDecimal currentInvestmentCorpus,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal monthlyContribution,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal annualExpectedReturnPercentage,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal annualInflationPercentage,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal annualContributionGrowthPercentage,

        @NotNull
        @Min(1)
        @Max(60)
        Integer yearsToProject,

        @NotNull
        @Min(1)
        @Max(100)
        Integer currentAge,

        @NotNull
        @Min(1)
        @Max(100)
        Integer retirementAge,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal currentMonthlyRetirementExpenses
) {
}
