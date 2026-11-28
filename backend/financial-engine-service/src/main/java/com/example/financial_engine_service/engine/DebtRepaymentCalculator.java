package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.debt.DebtRepaymentProjection;
import com.example.financial_engine_service.enums.DebtRepaymentCalculationStatus;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Projects a debt repayment schedule using a nominal annual interest rate and a fixed
 * monthly EMI. Interest is accrued monthly and rounded to the currency's two decimal
 * places before the EMI is applied. Payments are assumed to occur on the scheduled
 * monthly due date, beginning with nextPaymentDate.
 *
 * <p>This is an estimate, not a lender payoff quote. It does not model fees, rate resets,
 * prepayments, missed payments, floating-rate changes, or lender-specific day-count rules.</p>
 */
public final class DebtRepaymentCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final int MONEY_SCALE = 2;
    private static final int MAX_AMORTIZATION_MONTHS = 1_800;

    /**
     * Calculates estimated months to payoff and, optionally, the balance after a requested
     * number of monthly payments. A missing next-payment date does not prevent a duration
     * estimate, but the calendar payoff date remains null.
     *
     * @param outstandingAmount current principal outstanding
     * @param annualInterestRate nominal annual interest rate in percent (for example 8.5)
     * @param monthlyEmi fixed monthly instalment
     * @param nextPaymentDate date of the next scheduled payment, if known
     * @param asOfDate date on which the supplied balance and EMI are considered current
     * @param projectionMonths optional number of monthly payments after which to report balance
     */
    public DebtRepaymentProjection calculate(
            BigDecimal outstandingAmount,
            BigDecimal annualInterestRate,
            BigDecimal monthlyEmi,
            LocalDate nextPaymentDate,
            LocalDate asOfDate,
            Integer projectionMonths
    ) {
        List<String> warnings = new ArrayList<>();

        if (asOfDate == null) {
            return insufficient("An as-of date is required to calculate a repayment projection.", projectionMonths);
        }
        if (outstandingAmount == null) {
            return insufficient("Outstanding amount is missing.", projectionMonths);
        }
        if (outstandingAmount.signum() < 0) {
            return insufficient("Outstanding amount cannot be negative.", projectionMonths);
        }
        if (projectionMonths != null && projectionMonths < 0) {
            return insufficient("Projection months cannot be negative.", projectionMonths);
        }

        BigDecimal principal = money(outstandingAmount);
        if (principal.signum() == 0) {
            if (projectionMonths != null && projectionMonths > 0) {
                warnings.add("The liability has no outstanding principal; projected balance remains zero.");
            }
            return new DebtRepaymentProjection(
                    0, asOfDate, ZERO, projectionMonths,
                    projectionMonths == null ? null : ZERO,
                    DebtRepaymentCalculationStatus.PAID_OFF, warnings);
        }

        if (annualInterestRate == null) {
            return insufficient("Annual interest rate is missing; repayment duration cannot be estimated.", projectionMonths);
        }
        if (monthlyEmi == null) {
            return insufficient("Monthly EMI is missing; repayment duration cannot be estimated.", projectionMonths);
        }
        if (annualInterestRate.signum() < 0) {
            return insufficient("Annual interest rate cannot be negative.", projectionMonths);
        }
        if (monthlyEmi.signum() <= 0) {
            return insufficient("Monthly EMI must be greater than zero.", projectionMonths);
        }

        BigDecimal emi = money(monthlyEmi);
        if (emi.signum() <= 0) {
            return insufficient("Monthly EMI rounds to zero at currency precision.", projectionMonths);
        }

        BigDecimal monthlyRate = annualInterestRate
                .divide(ONE_HUNDRED, MC)
                .divide(TWELVE, MC);

        BigDecimal balance = principal;
        Integer monthsToPayoff = null;
        BigDecimal finalPayment = null;
        BigDecimal projectedBalance = projectionMonths != null && projectionMonths == 0
                ? balance : null;
        boolean nonAmortizing = false;
        boolean payoffFound = false;

        for (int month = 1; month <= MAX_AMORTIZATION_MONTHS; month++) {
            BigDecimal interest = balance.multiply(monthlyRate, MC)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal amountDue = balance.add(interest).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            if (emi.compareTo(amountDue) >= 0) {
                monthsToPayoff = month;
                finalPayment = amountDue;
                balance = ZERO;
                payoffFound = true;
            } else {
                if (emi.compareTo(interest) <= 0) {
                    nonAmortizing = true;
                }
                balance = amountDue.subtract(emi).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            }

            if (projectionMonths != null && month == projectionMonths) {
                projectedBalance = balance;
            }

            if (payoffFound) {
                if (projectionMonths != null && projectionMonths > month) {
                    projectedBalance = ZERO;
                }
                break;
            }

            if (nonAmortizing && (projectionMonths == null || month >= projectionMonths)) {
                // A non-amortizing balance will not become amortizing under fixed rate/EMI.
                // Stop once the requested projection horizon has been reached.
                break;
            }
        }

        if (projectionMonths != null && projectionMonths == 0) {
            projectedBalance = principal;
        }

        if (projectionMonths != null && projectionMonths > MAX_AMORTIZATION_MONTHS
                && !payoffFound) {
            warnings.add("Requested balance horizon exceeds the supported projection limit of "
                    + MAX_AMORTIZATION_MONTHS + " months.");
            projectedBalance = null;
        }

        LocalDate payoffDate = null;
        if (monthsToPayoff != null) {
            if (nextPaymentDate == null) {
                warnings.add("Next payment date is missing; payoff duration is estimated but calendar payoff date is unavailable.");
            } else if (nextPaymentDate.isBefore(asOfDate)) {
                warnings.add("Next payment date is before the as-of date; calendar payoff date is not estimated.");
            } else {
                payoffDate = nextPaymentDate.plusMonths(monthsToPayoff - 1L);
            }
        }

        DebtRepaymentCalculationStatus status;
        if (nonAmortizing) {
            status = DebtRepaymentCalculationStatus.NON_AMORTIZING;
            warnings.add("The EMI does not exceed monthly interest at the current balance; the principal is not reducing under this estimate.");
        } else if (payoffFound) {
            status = DebtRepaymentCalculationStatus.CALCULATED;
        } else {
            status = DebtRepaymentCalculationStatus.MAXIMUM_TERM_EXCEEDED;
            warnings.add("The liability was not fully repaid within the supported projection limit of "
                    + MAX_AMORTIZATION_MONTHS + " months.");
        }

        return new DebtRepaymentProjection(
                monthsToPayoff,
                payoffDate,
                finalPayment,
                projectionMonths,
                projectedBalance == null ? null : money(projectedBalance),
                status,
                warnings
        );
    }

    private DebtRepaymentProjection insufficient(String warning, Integer projectionMonths) {
        return new DebtRepaymentProjection(
                null, null, null, projectionMonths, null,
                DebtRepaymentCalculationStatus.INSUFFICIENT_DATA,
                List.of(warning)
        );
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
