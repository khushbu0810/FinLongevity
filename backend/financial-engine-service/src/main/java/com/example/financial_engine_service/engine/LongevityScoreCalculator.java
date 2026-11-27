package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.*;
import com.example.financial_engine_service.enums.FinancialHealthBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class LongevityScoreCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public LongevityScoreResponse calculate(
            FinancialSnapshotRequest request,
            CashFlowAnalysisResponse cashFlow,
            NetWorthAnalysisResponse netWorth,
            BigDecimal emergencyFundMonths) {

        BigDecimal savings = savingsComponent(cashFlow.savingsRatePercentage());
        BigDecimal emergency = emergencyFundComponent(emergencyFundMonths);
        BigDecimal debt = debtBurdenComponent(
                cashFlow.monthlyIncome(), cashFlow.monthlyDebtPayments());
        BigDecimal assets = netWorthComponent(netWorth.netWorth());

        int total = savings.add(emergency).add(debt).add(assets)
                .setScale(0, RoundingMode.HALF_UP).intValue();
        total = Math.max(0, Math.min(100, total));

        FinancialHealthBand band = determineBand(total);
        String explanation = "Score is an illustrative indicator based on savings rate, "
                + "emergency-fund coverage, debt-payment burden and net worth. "
                + "It is not a credit score or investment recommendation.";

        return new LongevityScoreResponse(
                total, band, savings, emergency, debt, assets, explanation);
    }

    private BigDecimal savingsComponent(BigDecimal savingsRate) {
        BigDecimal rate = savingsRate == null ? ZERO : savingsRate;
        BigDecimal points;
        if (rate.compareTo(ZERO) <= 0) points = ZERO;
        else if (rate.compareTo(BigDecimal.valueOf(10)) < 0) points = BigDecimal.valueOf(10);
        else if (rate.compareTo(BigDecimal.valueOf(20)) < 0) points = BigDecimal.valueOf(20);
        else points = BigDecimal.valueOf(30);
        return points;
    }

    private BigDecimal emergencyFundComponent(BigDecimal months) {
        if (months == null || months.compareTo(ZERO) <= 0) return ZERO;
        if (months.compareTo(BigDecimal.valueOf(1)) < 0) return BigDecimal.valueOf(5);
        if (months.compareTo(BigDecimal.valueOf(3)) < 0) return BigDecimal.valueOf(15);
        if (months.compareTo(BigDecimal.valueOf(6)) < 0) return BigDecimal.valueOf(24);
        return BigDecimal.valueOf(30);
    }

    private BigDecimal debtBurdenComponent(BigDecimal income, BigDecimal debtPayments) {
        if (income == null || income.compareTo(ZERO) <= 0) return ZERO;
        BigDecimal debtRatio = (debtPayments == null ? ZERO : debtPayments)
                .multiply(HUNDRED)
                .divide(income, 2, RoundingMode.HALF_UP);

        if (debtRatio.compareTo(BigDecimal.valueOf(10)) <= 0) return BigDecimal.valueOf(20);
        if (debtRatio.compareTo(BigDecimal.valueOf(20)) <= 0) return BigDecimal.valueOf(15);
        if (debtRatio.compareTo(BigDecimal.valueOf(35)) <= 0) return BigDecimal.valueOf(8);
        return ZERO;
    }

    private BigDecimal netWorthComponent(BigDecimal netWorth) {
        if (netWorth == null || netWorth.compareTo(ZERO) <= 0) return ZERO;
        return BigDecimal.valueOf(20);
    }

    private FinancialHealthBand determineBand(int score) {
        if (score < 25) return FinancialHealthBand.CRITICAL;
        if (score < 45) return FinancialHealthBand.NEEDS_IMPROVEMENT;
        if (score < 65) return FinancialHealthBand.FAIR;
        if (score < 85) return FinancialHealthBand.GOOD;
        return FinancialHealthBand.EXCELLENT;
    }
}
