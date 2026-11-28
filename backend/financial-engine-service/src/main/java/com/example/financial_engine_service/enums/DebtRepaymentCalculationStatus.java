package com.example.financial_engine_service.enums;

/** Result of projecting a liability's repayment using its current balance, rate and EMI. */
public enum DebtRepaymentCalculationStatus {
    PAID_OFF,
    CALCULATED,
    NON_AMORTIZING,
    INSUFFICIENT_DATA,
    MAXIMUM_TERM_EXCEEDED
}
