package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.retirement.RetirementExpenseCategoryProjectionResponse;
import com.example.financial_engine_service.dto.retirement.RetirementExpenseRequest;
import com.example.financial_engine_service.enums.RetirementExpenseCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/** Projects retirement expenses using a separate inflation assumption for each expense line. */
@Component
public class RetirementExpenseCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int MONEY_SCALE = 2;

    /**
     * Projects all supplied expense lines to targetAge and returns a monthly total.
     * If no categorized lines are supplied, the legacy total expense and general
     * inflation inputs are used so existing clients retain their previous behaviour.
     */
    public ExpenseProjection projectMonthlyExpensesAtAge(
            List<RetirementExpenseRequest> expenses,
            BigDecimal legacyCurrentMonthlyExpenses,
            BigDecimal legacyAnnualInflationPercentage,
            int currentAge,
            int targetAge) {

        Objects.requireNonNull(legacyCurrentMonthlyExpenses, "legacyCurrentMonthlyExpenses must not be null");
        Objects.requireNonNull(legacyAnnualInflationPercentage,
                "legacyAnnualInflationPercentage must not be null");
        if (targetAge < currentAge) {
            throw new IllegalArgumentException("Target age cannot be less than current age");
        }

        if (expenses == null || expenses.isEmpty()) {
            RetirementExpenseRequest legacy = new RetirementExpenseRequest(
                    RetirementExpenseCategory.OTHER,
                    "Current retirement expenses",
                    legacyCurrentMonthlyExpenses,
                    legacyAnnualInflationPercentage);
            return projectLines(List.of(legacy), currentAge, targetAge);
        }

        return projectLines(expenses, currentAge, targetAge);
    }

    private ExpenseProjection projectLines(
            List<RetirementExpenseRequest> expenses, int currentAge, int targetAge) {
        int years = targetAge - currentAge;
        List<RetirementExpenseCategoryProjectionResponse> projections = expenses.stream()
                .map(expense -> projectLine(expense, years))
                .toList();

        // Sum the rounded line items so the displayed category breakdown reconciles
        // exactly with the displayed monthly total.
        BigDecimal total = projections.stream()
                .map(RetirementExpenseCategoryProjectionResponse::projectedMonthlyAmount)
                .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE), BigDecimal::add);

        return new ExpenseProjection(total, projections);
    }

    private RetirementExpenseCategoryProjectionResponse projectLine(
            RetirementExpenseRequest expense, int years) {
        Objects.requireNonNull(expense.category(), "expense category must not be null");
        Objects.requireNonNull(expense.name(), "expense name must not be null");
        Objects.requireNonNull(expense.currentMonthlyAmount(), "current monthly amount must not be null");
        Objects.requireNonNull(expense.annualInflationPercentage(), "inflation percentage must not be null");

        if (expense.currentMonthlyAmount().signum() < 0) {
            throw new IllegalArgumentException("Expense amount cannot be negative: " + expense.name());
        }
        if (expense.annualInflationPercentage().signum() < 0
                || expense.annualInflationPercentage().compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Annual inflation must be between 0 and 100: " + expense.name());
        }

        BigDecimal annualInflation = expense.annualInflationPercentage().divide(HUNDRED, MC);
        BigDecimal growthFactor = ONE.add(annualInflation, MC).pow(years, MC);
        BigDecimal projected = expense.currentMonthlyAmount().multiply(growthFactor, MC);

        return new RetirementExpenseCategoryProjectionResponse(
                expense.category(), expense.name(), money(expense.currentMonthlyAmount()),
                money(expense.annualInflationPercentage()), money(projected));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public record ExpenseProjection(
            BigDecimal totalProjectedMonthlyExpenses,
            List<RetirementExpenseCategoryProjectionResponse> breakdown) {
        public ExpenseProjection {
            breakdown = List.copyOf(breakdown);
        }
    }
}
