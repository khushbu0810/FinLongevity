package com.example.financial_engine_service.enums;

/** Recurring income categories. Corpus-based products such as EPF, PPF and NPS
 * balances are deliberately not represented as income types. */
public enum RetirementIncomeType {
    EPS_PENSION,
    EMPLOYER_PENSION,
    NPS_ANNUITY,
    OTHER_ANNUITY,
    RENTAL_INCOME,
    OTHER_RECURRING_INCOME
}
