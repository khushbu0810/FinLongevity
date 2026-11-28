package com.example.financial_engine_service.dto.debt;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** Inputs required by the Financial Engine to assess liabilities against retirement. */
public record DebtRetirementAssessmentRequest(
        @NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a three-letter uppercase code")
        String currency,
        @NotNull @Min(0) @Max(120)
        Integer currentAge,
        @Min(1) @Max(120)
        Integer plannedRetirementAge,
        @NotNull
        LocalDate asOfDate
) {
}
