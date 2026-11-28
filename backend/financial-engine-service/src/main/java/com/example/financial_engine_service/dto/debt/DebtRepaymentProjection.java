package com.example.financial_engine_service.dto.debt;

import com.example.financial_engine_service.enums.DebtRepaymentCalculationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Result of projecting one liability's repayment schedule. A null payoff date means
 * the payment calendar was not sufficiently known; it does not mean the loan never ends.
 */
public record DebtRepaymentProjection(
        Integer estimatedMonthsToPayoff,
        LocalDate estimatedPayoffDate,
        BigDecimal estimatedFinalPaymentAmount,
        Integer projectionMonths,
        BigDecimal projectedBalanceAfterMonths,
        DebtRepaymentCalculationStatus calculationStatus,
        List<String> warnings
) {
    public DebtRepaymentProjection {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
