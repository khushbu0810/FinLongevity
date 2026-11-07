package com.example.customer_service.dto.income;

import com.example.customer_service.model.income.IncomeFrequency;
import com.example.customer_service.model.income.IncomeType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record IncomeSourceRequest(
        @NotNull
        IncomeType incomeType,

        @NotBlank
        @Size(max = 120)
        String sourceName,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotNull
        IncomeFrequency frequency,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}")
        String currency,

        boolean active
) {
}
