package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.retirement.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class RetirementProjectionCalculator {

    private static final int SCALE = 2;
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final RetirementAccumulationCalculator accumulationCalculator;
    private final RetirementIncomeCalculator incomeCalculator;
    private final RetirementExpenseCalculator expenseCalculator;
    private final RetirementDecumulationCalculator decumulationCalculator;
    private final RetirementCorpusRequirementCalculator corpusRequirementCalculator;

    public RetirementProjectionCalculator() {
        this(new RetirementAccumulationCalculator(), new RetirementIncomeCalculator(),
                new RetirementExpenseCalculator());
    }

    public RetirementProjectionCalculator(RetirementAccumulationCalculator accumulationCalculator,
                                          RetirementIncomeCalculator incomeCalculator) {
        this(accumulationCalculator, incomeCalculator, new RetirementExpenseCalculator());
    }

    public RetirementProjectionCalculator(RetirementAccumulationCalculator accumulationCalculator,
                                          RetirementIncomeCalculator incomeCalculator,
                                          RetirementExpenseCalculator expenseCalculator) {
        this(accumulationCalculator, incomeCalculator, expenseCalculator,
                new RetirementDecumulationCalculator(incomeCalculator, expenseCalculator));
    }

    public RetirementProjectionCalculator(RetirementAccumulationCalculator accumulationCalculator,
                                          RetirementIncomeCalculator incomeCalculator,
                                          RetirementExpenseCalculator expenseCalculator,
                                          RetirementDecumulationCalculator decumulationCalculator) {
        this(accumulationCalculator, incomeCalculator, expenseCalculator, decumulationCalculator,
                new RetirementCorpusRequirementCalculator(incomeCalculator, expenseCalculator));
    }

    @Autowired
    public RetirementProjectionCalculator(RetirementAccumulationCalculator accumulationCalculator,
                                          RetirementIncomeCalculator incomeCalculator,
                                          RetirementExpenseCalculator expenseCalculator,
                                          RetirementDecumulationCalculator decumulationCalculator,
                                          RetirementCorpusRequirementCalculator corpusRequirementCalculator) {
        this.accumulationCalculator = accumulationCalculator;
        this.incomeCalculator = incomeCalculator;
        this.expenseCalculator = expenseCalculator;
        this.decumulationCalculator = decumulationCalculator;
        this.corpusRequirementCalculator = corpusRequirementCalculator;
    }

    public RetirementProjectionResponse calculate(RetirementProjectionRequest request, String currency) {
        BigDecimal corpus = request.currentInvestmentCorpus();
        BigDecimal contribution = request.monthlyContribution();
        BigDecimal monthlyRate = accumulationCalculator.effectiveMonthlyRate(
                request.annualExpectedReturnPercentage());
        BigDecimal contributionGrowth = request.annualContributionGrowthPercentage()
                .divide(HUNDRED, MC);
        BigDecimal totalContributions = ZERO;
        BigDecimal monthlyExpense = request.currentMonthlyRetirementExpenses();
        RetirementExpenseCalculator.ExpenseProjection retirementExpenseProjection = null;
        List<RetirementProjectionPointResponse> points = new ArrayList<>();

        for (int year = 1; year <= request.yearsToProject(); year++) {
            BigDecimal yearlyContribution = contribution.multiply(TWELVE);
            for (int month = 0; month < 12; month++) {
                corpus = accumulationCalculator.applyEndOfMonthContribution(
                        corpus, contribution, monthlyRate);
            }

            totalContributions = totalContributions.add(yearlyContribution, MC);
            int projectedAge = request.currentAge() + year;
            retirementExpenseProjection = expenseCalculator.projectMonthlyExpensesAtAge(
                    request.retirementExpenses(), request.currentMonthlyRetirementExpenses(),
                    request.annualInflationPercentage(), request.currentAge(), projectedAge);
            monthlyExpense = retirementExpenseProjection.totalProjectedMonthlyExpenses();
            BigDecimal monthlyIncome = incomeCalculator.calculateMonthlyIncomeAtAge(
                    request.retirementIncomeStreams(), request.currentAge(), projectedAge);
            BigDecimal monthlyGap = monthlyExpense.subtract(monthlyIncome).max(ZERO);
            BigDecimal monthlySurplus = monthlyIncome.subtract(monthlyExpense).max(ZERO);

            points.add(new RetirementProjectionPointResponse(
                    year, projectedAge, money(corpus), money(monthlyExpense),
                    money(monthlyExpense.multiply(TWELVE)), money(totalContributions),
                    money(monthlyIncome), money(monthlyGap), money(monthlySurplus),
                    retirementExpenseProjection.breakdown()));

            contribution = accumulationCalculator.growContributionForNextYear(
                    contribution, contributionGrowth);
        }

        BigDecimal retirementIncome = incomeCalculator.calculateMonthlyIncomeAtAge(
                request.retirementIncomeStreams(), request.currentAge(), request.retirementAge());
        BigDecimal retirementIncomeGap = monthlyExpense.subtract(retirementIncome).max(ZERO);
        BigDecimal retirementIncomeSurplus = retirementIncome.subtract(monthlyExpense).max(ZERO);
        List<RetirementIncomeStreamProjectionResponse> streamResults =
                incomeCalculator.projectAtRetirementAge(request.retirementIncomeStreams(),
                        request.currentAge(), request.retirementAge());

        RetirementDecumulationResponse decumulation = decumulationCalculator.calculate(
                corpus, request.annualExpectedReturnPercentage(), request.currentAge(),
                request.retirementAge(), request.retirementPlanningAge(),
                request.retirementIncomeStreams(), request.retirementExpenses(),
                request.currentMonthlyRetirementExpenses(), request.annualInflationPercentage());

        RetirementCorpusRequirementResponse corpusRequirement = corpusRequirementCalculator.calculate(
                corpus, request.annualExpectedReturnPercentage(), request.currentAge(),
                request.retirementAge(), request.retirementPlanningAge(),
                request.retirementIncomeStreams(), request.retirementExpenses(),
                request.currentMonthlyRetirementExpenses(), request.annualInflationPercentage());
        BigDecimal requiredCorpus = corpusRequirement.requiredCorpusAtRetirement();

        return new RetirementProjectionResponse(
                request.currentAge(), request.retirementAge(), request.yearsToProject(),
                money(request.currentInvestmentCorpus()), money(totalContributions), money(corpus),
                money(monthlyExpense), money(monthlyExpense.multiply(TWELVE)), money(requiredCorpus),
                corpusRequirement.projectedCorpusMeetsRequirement(), currency, List.copyOf(points),
                money(retirementIncome), money(retirementIncomeGap), money(retirementIncomeSurplus),
                streamResults, retirementExpenseProjection == null
                        ? List.of() : retirementExpenseProjection.breakdown(), decumulation, corpusRequirement);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
