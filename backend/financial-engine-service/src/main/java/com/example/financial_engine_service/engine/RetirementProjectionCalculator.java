package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.*;
import com.example.financial_engine_service.dto.retirement.*;
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
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public RetirementProjectionResponse calculate(RetirementProjectionRequest request,
                                                  String currency) {
        BigDecimal corpus = request.currentInvestmentCorpus();
        BigDecimal contribution = request.monthlyContribution();
        BigDecimal annualReturn = request.annualExpectedReturnPercentage()
                .divide(HUNDRED, MC);
        BigDecimal monthlyRate = annualReturn.divide(TWELVE, MC);
        BigDecimal contributionGrowth = request.annualContributionGrowthPercentage()
                .divide(HUNDRED, MC);
        BigDecimal inflation = request.annualInflationPercentage().divide(HUNDRED, MC);

        BigDecimal totalContributions = BigDecimal.ZERO;
        BigDecimal monthlyExpense = request.currentMonthlyRetirementExpenses();
        List<RetirementProjectionPointResponse> points = new ArrayList<>();

        for (int year = 1; year <= request.yearsToProject(); year++) {
            BigDecimal yearlyContribution = contribution.multiply(TWELVE);
            for (int month = 0; month < 12; month++) {
                corpus = corpus.multiply(ONE.add(monthlyRate), MC)
                        .add(contribution, MC);
            }
            totalContributions = totalContributions.add(yearlyContribution);
            monthlyExpense = monthlyExpense.multiply(ONE.add(inflation), MC);
            points.add(new RetirementProjectionPointResponse(
                    year,
                    request.currentAge() + year,
                    money(corpus),
                    money(monthlyExpense),
                    money(monthlyExpense.multiply(TWELVE)),
                    money(totalContributions)
            ));
            contribution = contribution.multiply(ONE.add(contributionGrowth), MC);
        }

        BigDecimal requiredCorpus = monthlyExpense.multiply(TWELVE)
                .multiply(BigDecimal.valueOf(25)); // illustrative 25x annual expense heuristic

        return new RetirementProjectionResponse(
                request.currentAge(),
                request.retirementAge(),
                request.yearsToProject(),
                money(request.currentInvestmentCorpus()),
                money(totalContributions),
                money(corpus),
                money(monthlyExpense),
                money(monthlyExpense.multiply(TWELVE)),
                money(requiredCorpus),
                corpus.compareTo(requiredCorpus) >= 0,
                currency,
                List.copyOf(points)
        );
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
