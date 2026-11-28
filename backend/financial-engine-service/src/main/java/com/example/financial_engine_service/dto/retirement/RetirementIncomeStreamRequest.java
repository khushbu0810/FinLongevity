package com.example.financial_engine_service.dto.retirement;

import com.example.financial_engine_service.enums.RetirementIncomeFrequency;
import com.example.financial_engine_service.enums.RetirementIncomeType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * An income amount expressed in today's currency and at the supplied frequency.
 * Annual escalation is applied from the user's current age through the projection age.
 */
public record RetirementIncomeStreamRequest(
        @NotNull RetirementIncomeType type,
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotNull RetirementIncomeFrequency frequency,
        @NotNull @DecimalMin("-100.00") @DecimalMax("100.00") BigDecimal annualGrowthPercentage,
        @NotNull @Min(1) @Max(120) Integer startsAtAge,
        @Min(1) @Max(120) Integer endsAtAge
) {
    /** Alternative argument order for readable construction in tests and clients. */
    public RetirementIncomeStreamRequest(String name, RetirementIncomeType type,
            BigDecimal amount, RetirementIncomeFrequency frequency,
            Integer startsAtAge, Integer endsAtAge, BigDecimal annualGrowthPercentage) {
        this(type, name, amount, frequency, annualGrowthPercentage, startsAtAge, endsAtAge);
    }
}
