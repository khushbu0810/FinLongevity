package com.example.goal_service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record GoalContributionRequest(
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency,

        @Size(max = 500)
        String note
) {
}
