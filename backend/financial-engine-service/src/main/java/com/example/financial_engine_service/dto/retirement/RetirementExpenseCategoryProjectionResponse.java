package com.example.financial_engine_service.dto.retirement;

import com.example.financial_engine_service.enums.RetirementExpenseCategory;

import java.math.BigDecimal;

/** Projected monthly amount for one retirement expense line at a target age. */
public record RetirementExpenseCategoryProjectionResponse(
        RetirementExpenseCategory category,
        String name,
        BigDecimal currentMonthlyAmount,
        BigDecimal annualInflationPercentage,
        BigDecimal projectedMonthlyAmount
) {
}
