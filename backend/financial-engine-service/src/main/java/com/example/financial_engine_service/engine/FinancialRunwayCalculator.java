package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.FinancialSnapshotRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FinancialRunwayCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public BigDecimal calculateEmergencyFundMonths(FinancialSnapshotRequest request) {
        BigDecimal monthlyExpenses = safe(request.monthlyExpenses());
        if (monthlyExpenses.compareTo(ZERO) <= 0) {
            return null;
        }
        return safe(request.liquidAssets())
                .divide(monthlyExpenses, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFinancialRunwayMonths(FinancialSnapshotRequest request) {
        BigDecimal monthlyBurn = safe(request.monthlyExpenses())
                .add(safe(request.monthlyDebtPayments()));

        if (monthlyBurn.compareTo(ZERO) <= 0) {
            return null;
        }
        return safe(request.liquidAssets())
                .divide(monthlyBurn, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
