package com.example.financial_engine_service.dto;

import com.example.financial_engine_service.enums.AnalysisType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FinancialAnalysisRequest(
        @NotNull
        AnalysisType analysisType,

        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase ISO code")
        @Size(min = 3, max = 3)
        String baseCurrency
) {
}
