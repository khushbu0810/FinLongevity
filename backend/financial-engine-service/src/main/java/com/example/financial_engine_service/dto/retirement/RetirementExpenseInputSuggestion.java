package com.example.financial_engine_service.dto.retirement;

import com.example.financial_engine_service.enums.RetirementExpenseCategory;

import java.math.BigDecimal;

/**
 * A suggested retirement expense line mapped from an active customer expense.
 * Inflation is deliberately not inferred; the customer must review and choose it.
 */
public record RetirementExpenseInputSuggestion(
        String sourceExpenseId,
        String sourceExpenseName,
        String sourceExpenseCategory,
        String sourceFrequency,
        RetirementExpenseCategory suggestedCategory,
        BigDecimal currentMonthlyAmount,
        boolean markedEssential
) {
}
