package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.FinancialSnapshotRequest;
import com.example.financial_engine_service.dto.CashFlowAnalysisResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CashFlowCalculator {

    private static final int SCALE = 2;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public CashFlowAnalysisResponse calculate(FinancialSnapshotRequest request) {
        BigDecimal income = safe(request.monthlyIncome());
        BigDecimal expenses = safe(request.monthlyExpenses());
        BigDecimal debtPayments = safe(request.monthlyDebtPayments());

        BigDecimal surplus = income.subtract(expenses).subtract(debtPayments)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal savingsRate = income.compareTo(ZERO) > 0
                ? surplus.multiply(BigDecimal.valueOf(100))
                .divide(income, SCALE, RoundingMode.HALF_UP)
                : ZERO.setScale(SCALE);

        return new CashFlowAnalysisResponse(
                income.setScale(SCALE, RoundingMode.HALF_UP),
                expenses.setScale(SCALE, RoundingMode.HALF_UP),
                debtPayments.setScale(SCALE, RoundingMode.HALF_UP),
                surplus,
                savingsRate,
                surplus.compareTo(ZERO) > 0,
                request.currency()
        );
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
