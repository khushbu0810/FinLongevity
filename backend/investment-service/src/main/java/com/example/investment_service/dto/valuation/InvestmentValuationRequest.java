package com.example.investment_service.dto.valuation;

import com.example.investment_service.model.valuation.InvestmentValuationSource;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestmentValuationRequest(
        @NotNull(message = "Market value is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Market value cannot be negative")
        @Digits(integer = 15, fraction = 4, message = "Market value supports up to 15 integer and 4 decimal digits")
        BigDecimal marketValue,

        @NotNull(message = "Valuation date is required")
        @PastOrPresent(message = "Valuation date cannot be in the future")
        LocalDate valuationDate,

        @NotNull(message = "Valuation source is required")
        InvestmentValuationSource valuationSource,

        @Size(max = 200, message = "Source reference cannot exceed 200 characters")
        String sourceReference
) {
}
