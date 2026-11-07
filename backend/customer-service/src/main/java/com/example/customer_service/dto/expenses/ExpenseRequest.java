package com.example.customer_service.dto.expenses;

import com.example.customer_service.model.expenses.ExpenseCategory;
import com.example.customer_service.model.expenses.ExpenseFrequency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @NotNull
        ExpenseCategory category,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter code")
        String currency,

        @NotNull
        ExpenseFrequency frequency,

        @NotNull
        LocalDate startDate,

        LocalDate endDate,

        boolean essential,

        boolean active,

        @Size(max = 500)
        String notes
) {
}
