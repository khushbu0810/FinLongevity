package com.example.financial_engine_service.dto.debt;

/** Outcome of assessing one customer liability against a planned retirement date. */
public enum DebtRetirementAssessmentStatus {
    PAID_OFF,
    ESTIMATED_PAID_OFF_BEFORE_RETIREMENT,
    ESTIMATED_CONTINUES_INTO_RETIREMENT,
    NON_AMORTIZING,
    INSUFFICIENT_DATA,
    RETIREMENT_AGE_NOT_PROVIDED
}
