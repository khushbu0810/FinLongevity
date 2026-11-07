package com.example.customer_service.dto.asset_liabilities;

import com.example.customer_service.model.asset_liabilities.LiabilityCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LiabilityRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotNull
        LiabilityCategory category,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal outstandingAmount,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase code")
        String currency,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal annualInterestRate,

        @DecimalMin("0.0")
        BigDecimal monthlyEmi,

        @FutureOrPresent
        LocalDate nextPaymentDate,

        @Size(max = 500)
        String notes
) {
}
