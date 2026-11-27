package com.example.financial_engine_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record FinancialSnapshotRequest(
        @DecimalMin(value = "0.00", message = "Monthly income cannot be negative")
        BigDecimal monthlyIncome,

        @DecimalMin(value = "0.00", message = "Monthly expenses cannot be negative")
        BigDecimal monthlyExpenses,

        @DecimalMin(value = "0.00", message = "Monthly debt payments cannot be negative")
        BigDecimal monthlyDebtPayments,

        @DecimalMin(value = "0.00", message = "Total assets cannot be negative")
        BigDecimal totalAssets,

        @DecimalMin(value = "0.00", message = "Total liabilities cannot be negative")
        BigDecimal totalLiabilities,

        @DecimalMin(value = "0.00", message = "Liquid assets cannot be negative")
        BigDecimal liquidAssets,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase ISO code")
        String currency
) {
}
