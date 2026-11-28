package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.retirement.RetirementDecumulationPointResponse;
import com.example.financial_engine_service.dto.retirement.RetirementDecumulationResponse;
import com.example.financial_engine_service.dto.retirement.RetirementExpenseRequest;
import com.example.financial_engine_service.dto.retirement.RetirementIncomeStreamRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Projects retirement corpus drawdown year by year.
 * Annual expenses are offset by recurring income. The remaining deficit is withdrawn
 * at the beginning of the year; investment return is then applied to the remaining
 * corpus. Income surplus is added to the corpus before annual growth.
 */
@Component
public class RetirementDecumulationCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 2;

    private final RetirementIncomeCalculator incomeCalculator;
    private final RetirementExpenseCalculator expenseCalculator;

    public RetirementDecumulationCalculator() {
        this(new RetirementIncomeCalculator(), new RetirementExpenseCalculator());
    }

    public RetirementDecumulationCalculator(RetirementIncomeCalculator incomeCalculator,
                                            RetirementExpenseCalculator expenseCalculator) {
        this.incomeCalculator = Objects.requireNonNull(incomeCalculator);
        this.expenseCalculator = Objects.requireNonNull(expenseCalculator);
    }

    public RetirementDecumulationResponse calculate(
            BigDecimal startingCorpus,
            BigDecimal annualExpectedReturnPercentage,
            int currentAge,
            int retirementAge,
            int planningAge,
            List<RetirementIncomeStreamRequest> incomeStreams,
            List<RetirementExpenseRequest> expenses,
            BigDecimal legacyCurrentMonthlyExpenses,
            BigDecimal legacyAnnualInflationPercentage) {

        Objects.requireNonNull(startingCorpus, "startingCorpus must not be null");
        Objects.requireNonNull(annualExpectedReturnPercentage, "annualExpectedReturnPercentage must not be null");
        if (startingCorpus.signum() < 0) {
            throw new IllegalArgumentException("Starting retirement corpus cannot be negative");
        }
        if (annualExpectedReturnPercentage.compareTo(ZERO) < 0
                || annualExpectedReturnPercentage.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Annual expected return must be between 0 and 100");
        }
        if (retirementAge <= currentAge || planningAge <= retirementAge || planningAge > 120) {
            throw new IllegalArgumentException("Planning age must be greater than retirement age, and retirement age must be greater than current age");
        }

        BigDecimal annualReturn = annualExpectedReturnPercentage.divide(HUNDRED, MC);
        BigDecimal corpus = startingCorpus;
        BigDecimal totalFundedWithdrawals = ZERO;
        BigDecimal totalUnfundedShortfall = ZERO;
        Integer exhaustionAge = null;
        List<RetirementDecumulationPointResponse> points = new ArrayList<>();

        for (int year = 1; year <= planningAge - retirementAge; year++) {
            int ageAtYearEnd = retirementAge + year;
            BigDecimal openingCorpus = corpus;

            RetirementExpenseCalculator.ExpenseProjection expenseProjection =
                    expenseCalculator.projectMonthlyExpensesAtAge(
                            expenses, legacyCurrentMonthlyExpenses, legacyAnnualInflationPercentage,
                            currentAge, ageAtYearEnd);
            BigDecimal annualExpenses = expenseProjection.totalProjectedMonthlyExpenses()
                    .multiply(TWELVE, MC);
            BigDecimal annualIncome = incomeCalculator.calculateMonthlyIncomeAtAge(
                    incomeStreams == null ? List.of() : incomeStreams, currentAge, ageAtYearEnd)
                    .multiply(TWELVE, MC);

            BigDecimal requiredWithdrawal = annualExpenses.subtract(annualIncome).max(ZERO);
            BigDecimal incomeSurplus = annualIncome.subtract(annualExpenses).max(ZERO);
            BigDecimal fundedWithdrawal = requiredWithdrawal.min(openingCorpus);
            BigDecimal unfundedShortfall = requiredWithdrawal.subtract(fundedWithdrawal).max(ZERO);

            BigDecimal corpusAfterCashFlow = openingCorpus.subtract(fundedWithdrawal, MC)
                    .add(incomeSurplus, MC);
            BigDecimal investmentGrowth = corpusAfterCashFlow.multiply(annualReturn, MC);
            corpus = corpusAfterCashFlow.add(investmentGrowth, MC).max(ZERO);

            totalFundedWithdrawals = totalFundedWithdrawals.add(fundedWithdrawal, MC);
            totalUnfundedShortfall = totalUnfundedShortfall.add(unfundedShortfall, MC);
            boolean exhausted = unfundedShortfall.signum() > 0
                    || (fundedWithdrawal.signum() > 0 && corpus.signum() == 0);
            if (exhausted && exhaustionAge == null) {
                exhaustionAge = ageAtYearEnd;
            }

            points.add(new RetirementDecumulationPointResponse(
                    year, ageAtYearEnd, money(openingCorpus), money(annualExpenses),
                    money(annualIncome), money(requiredWithdrawal), money(fundedWithdrawal),
                    money(unfundedShortfall), money(incomeSurplus), money(investmentGrowth),
                    money(corpus), exhausted));
        }

        return new RetirementDecumulationResponse(
                retirementAge, planningAge, money(startingCorpus), money(totalFundedWithdrawals),
                money(totalUnfundedShortfall), exhaustionAge, money(corpus),
                totalUnfundedShortfall.signum() == 0, points);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
