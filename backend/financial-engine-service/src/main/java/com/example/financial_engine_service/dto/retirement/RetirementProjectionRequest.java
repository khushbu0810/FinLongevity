package com.example.financial_engine_service.dto.retirement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RetirementProjectionRequest(
        @NotNull @DecimalMin("0.00") BigDecimal currentInvestmentCorpus,
        @NotNull @DecimalMin("0.00") BigDecimal monthlyContribution,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal annualExpectedReturnPercentage,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal annualInflationPercentage,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal annualContributionGrowthPercentage,
        @NotNull @Min(1) @Max(60) Integer yearsToProject,
        @NotNull @Min(1) @Max(100) Integer currentAge,
        @NotNull @Min(1) @Max(100) Integer retirementAge,
        @NotNull @DecimalMin("0.00") BigDecimal currentMonthlyRetirementExpenses,
        @Valid List<RetirementIncomeStreamRequest> retirementIncomeStreams,
        @Valid List<RetirementExpenseRequest> retirementExpenses,
        @Min(1) @Max(120) Integer retirementPlanningAge
) {
    public RetirementProjectionRequest {
        retirementIncomeStreams = retirementIncomeStreams == null ? List.of() : List.copyOf(retirementIncomeStreams);
        retirementExpenses = retirementExpenses == null ? List.of() : List.copyOf(retirementExpenses);
        retirementPlanningAge = retirementPlanningAge == null
                ? Math.max(90, retirementAge == null ? 90 : retirementAge + 1)
                : retirementPlanningAge;
    }

    /** Compatibility constructor for the previous canonical request shape. */
    public RetirementProjectionRequest(BigDecimal currentInvestmentCorpus, BigDecimal monthlyContribution,
            BigDecimal annualExpectedReturnPercentage, BigDecimal annualInflationPercentage,
            BigDecimal annualContributionGrowthPercentage, Integer yearsToProject, Integer currentAge,
            Integer retirementAge, BigDecimal currentMonthlyRetirementExpenses,
            List<RetirementIncomeStreamRequest> retirementIncomeStreams,
            List<RetirementExpenseRequest> retirementExpenses) {
        this(currentInvestmentCorpus, monthlyContribution, annualExpectedReturnPercentage,
                annualInflationPercentage, annualContributionGrowthPercentage, yearsToProject,
                currentAge, retirementAge, currentMonthlyRetirementExpenses,
                retirementIncomeStreams, retirementExpenses, null);
    }

    public RetirementProjectionRequest(BigDecimal currentInvestmentCorpus, BigDecimal monthlyContribution,
            BigDecimal annualExpectedReturnPercentage, BigDecimal annualInflationPercentage,
            BigDecimal annualContributionGrowthPercentage, Integer yearsToProject, Integer currentAge,
            Integer retirementAge, BigDecimal currentMonthlyRetirementExpenses,
            List<RetirementIncomeStreamRequest> retirementIncomeStreams) {
        this(currentInvestmentCorpus, monthlyContribution, annualExpectedReturnPercentage,
                annualInflationPercentage, annualContributionGrowthPercentage, yearsToProject,
                currentAge, retirementAge, currentMonthlyRetirementExpenses,
                retirementIncomeStreams, List.of(), null);
    }

    public RetirementProjectionRequest(BigDecimal currentInvestmentCorpus, BigDecimal monthlyContribution,
            BigDecimal annualExpectedReturnPercentage, BigDecimal annualInflationPercentage,
            BigDecimal annualContributionGrowthPercentage, Integer yearsToProject, Integer currentAge,
            Integer retirementAge, BigDecimal currentMonthlyRetirementExpenses) {
        this(currentInvestmentCorpus, monthlyContribution, annualExpectedReturnPercentage,
                annualInflationPercentage, annualContributionGrowthPercentage, yearsToProject,
                currentAge, retirementAge, currentMonthlyRetirementExpenses, List.of(), List.of(), null);
    }
}
