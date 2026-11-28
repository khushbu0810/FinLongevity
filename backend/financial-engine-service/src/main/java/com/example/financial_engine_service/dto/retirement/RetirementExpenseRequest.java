package com.example.financial_engine_service.dto.retirement;

import com.example.financial_engine_service.enums.RetirementExpenseCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * A recurring monthly expense expressed in today's currency.
 * The category-specific annual inflation assumption is applied from the current age
 * through the requested projection age.
 */
public record RetirementExpenseRequest(
        @NotNull RetirementExpenseCategory category,
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal currentMonthlyAmount,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal annualInflationPercentage
) {
}
