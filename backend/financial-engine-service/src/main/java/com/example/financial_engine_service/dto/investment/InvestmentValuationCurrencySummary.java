package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;

/** Currency-isolated totals for fresh valuation evidence. */
public record InvestmentValuationCurrencySummary(
        String currency,
        int investmentCount,
        int availableValuationCount,
        int staleValuationCount,
        int unavailableValuationCount,
        BigDecimal freshMarketValueTotal,
        boolean includedInCurrentRetirementCorpus
) {
}
