package com.example.goal_service.dto;

import com.example.goal_service.model.GoalType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialGoalRequest(
        @NotBlank
        @Size(max = 140)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        GoalType goalType,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal targetAmount,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency,

        @FutureOrPresent LocalDate targetDate
) {
}
