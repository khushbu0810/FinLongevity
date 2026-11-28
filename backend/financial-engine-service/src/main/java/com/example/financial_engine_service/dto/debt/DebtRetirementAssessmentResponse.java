package com.example.financial_engine_service.dto.debt;

import java.time.LocalDate;
import java.util.List;

/** Read-only debt assessment returned as part of retirement planning prefill. */
public record DebtRetirementAssessmentResponse(
        String currency,
        LocalDate asOfDate,
        Integer currentAge,
        Integer plannedRetirementAge,
        LocalDate estimatedRetirementDate,
        DebtRetirementAssessmentSummary summary,
        List<DebtRetirementLiabilityAssessment> liabilities,
        List<String> warnings
) {
    public DebtRetirementAssessmentResponse {
        liabilities = liabilities == null ? List.of() : List.copyOf(liabilities);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
