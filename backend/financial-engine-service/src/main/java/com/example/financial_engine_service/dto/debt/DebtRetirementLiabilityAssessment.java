package com.example.financial_engine_service.dto.debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Retirement-specific assessment of one existing Customer Service liability. */
public record DebtRetirementLiabilityAssessment(
        UUID liabilityId,
        String name,
        String category,
        BigDecimal outstandingAmount,
        String currency,
        BigDecimal annualInterestRate,
        BigDecimal monthlyEmi,
        LocalDate nextPaymentDate,
        Integer estimatedMonthsToPayoff,
        LocalDate estimatedPayoffDate,
        BigDecimal estimatedBalanceAtRetirement,
        Integer retirementOverlapMonths,
        DebtRetirementAssessmentStatus assessmentStatus,
        List<String> warnings
) {
    public DebtRetirementLiabilityAssessment {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
