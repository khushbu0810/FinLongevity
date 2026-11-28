package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.retirement.RetirementCorpusRequirementResponse;
import com.example.financial_engine_service.dto.retirement.RetirementExpenseRequest;
import com.example.financial_engine_service.dto.retirement.RetirementIncomeStreamRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calculates the minimum corpus required at retirement to fund the projected
 * annual expense-minus-income cash flows through the planning age.
 *
 * Cash-flow timing matches RetirementDecumulationCalculator: each year's net
 * cash need is paid at the beginning of that year and the remaining corpus earns
 * the annual return after the cash flow. The required opening corpus is therefore
 * the maximum non-negative prefix sum of discounted annual net cash needs.
 */
@Component
public class RetirementCorpusRequirementCalculator {

    private static final MathContext MC = new MathContext(24, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final int MONEY_SCALE = 2;
    private static final int PERCENT_SCALE = 2;

    private final RetirementIncomeCalculator incomeCalculator;
    private final RetirementExpenseCalculator expenseCalculator;

    public RetirementCorpusRequirementCalculator() {
        this(new RetirementIncomeCalculator(), new RetirementExpenseCalculator());
    }

    public RetirementCorpusRequirementCalculator(RetirementIncomeCalculator incomeCalculator,
                                                 RetirementExpenseCalculator expenseCalculator) {
        this.incomeCalculator = Objects.requireNonNull(incomeCalculator);
        this.expenseCalculator = Objects.requireNonNull(expenseCalculator);
    }

    public RetirementCorpusRequirementResponse calculate(
            BigDecimal projectedCorpusAtRetirement,
            BigDecimal annualExpectedReturnPercentage,
            int currentAge,
            int retirementAge,
            int planningAge,
            List<RetirementIncomeStreamRequest> incomeStreams,
            List<RetirementExpenseRequest> expenses,
            BigDecimal legacyCurrentMonthlyExpenses,
            BigDecimal legacyAnnualInflationPercentage) {

        Objects.requireNonNull(projectedCorpusAtRetirement, "projectedCorpusAtRetirement must not be null");
        Objects.requireNonNull(annualExpectedReturnPercentage, "annualExpectedReturnPercentage must not be null");
        Objects.requireNonNull(legacyCurrentMonthlyExpenses, "legacyCurrentMonthlyExpenses must not be null");
        Objects.requireNonNull(legacyAnnualInflationPercentage, "legacyAnnualInflationPercentage must not be null");

        if (projectedCorpusAtRetirement.signum() < 0) {
            throw new IllegalArgumentException("Projected retirement corpus cannot be negative");
        }
        if (annualExpectedReturnPercentage.signum() < 0
                || annualExpectedReturnPercentage.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Annual expected return must be between 0 and 100");
        }
        if (retirementAge <= currentAge || planningAge <= retirementAge || planningAge > 120) {
            throw new IllegalArgumentException("Planning age must be greater than retirement age, and retirement age must be greater than current age");
        }

        BigDecimal annualReturn = annualExpectedReturnPercentage.divide(HUNDRED, MC);
        BigDecimal discountFactor = ONE;
        BigDecimal cumulativeDiscountedCashNeed = ZERO;
        BigDecimal requiredCorpus = ZERO;

        for (int year = 1; year <= planningAge - retirementAge; year++) {
            // Keep age convention identical to decumulation: first retirement
            // cash-flow period is represented by retirementAge + 1.
            int ageAtYearEnd = retirementAge + year;
            BigDecimal annualExpenses = expenseCalculator.projectMonthlyExpensesAtAge(
                            expenses, legacyCurrentMonthlyExpenses, legacyAnnualInflationPercentage,
                            currentAge, ageAtYearEnd)
                    .totalProjectedMonthlyExpenses().multiply(TWELVE, MC);
            BigDecimal annualIncome = incomeCalculator.calculateMonthlyIncomeAtAge(
                            incomeStreams == null ? List.of() : incomeStreams, currentAge, ageAtYearEnd)
                    .multiply(TWELVE, MC);

            BigDecimal annualNetCashNeed = annualExpenses.subtract(annualIncome);
            cumulativeDiscountedCashNeed = cumulativeDiscountedCashNeed
                    .add(annualNetCashNeed.divide(discountFactor, MC), MC);
            requiredCorpus = requiredCorpus.max(cumulativeDiscountedCashNeed).max(ZERO);
            discountFactor = discountFactor.multiply(ONE.add(annualReturn, MC), MC);
        }

        BigDecimal surplus = projectedCorpusAtRetirement.subtract(requiredCorpus).max(ZERO);
        BigDecimal shortfall = requiredCorpus.subtract(projectedCorpusAtRetirement).max(ZERO);
        BigDecimal fundingRatio = requiredCorpus.signum() == 0
                ? HUNDRED
                : projectedCorpusAtRetirement.multiply(HUNDRED, MC).divide(requiredCorpus, MC);

        return new RetirementCorpusRequirementResponse(
                retirementAge,
                planningAge,
                money(requiredCorpus),
                money(projectedCorpusAtRetirement),
                money(surplus),
                money(shortfall),
                fundingRatio.setScale(PERCENT_SCALE, RoundingMode.HALF_UP),
                shortfall.signum() == 0);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
