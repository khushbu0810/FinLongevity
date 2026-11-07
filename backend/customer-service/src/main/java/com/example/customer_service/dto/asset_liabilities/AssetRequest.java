package com.example.customer_service.dto.asset_liabilities;

import com.example.customer_service.model.asset_liabilities.AssetCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotNull
        AssetCategory category,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal currentValue,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase code")
        String currency,

        @NotNull
        @PastOrPresent
        LocalDate valuationDate,

        boolean liquid,

        @Size(max = 500)
        String notes
) {
}
