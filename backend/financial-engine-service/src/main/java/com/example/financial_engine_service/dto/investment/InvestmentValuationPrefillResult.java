package com.example.financial_engine_service.dto.investment;

import java.util.List;

public record InvestmentValuationPrefillResult(
        List<InvestmentValuationPrefillItem> investments,
        List<InvestmentValuationCurrencySummary> summariesByCurrency,
        List<String> warnings
) {
    public InvestmentValuationPrefillResult {
        investments = investments == null ? List.of() : List.copyOf(investments);
        summariesByCurrency = summariesByCurrency == null ? List.of() : List.copyOf(summariesByCurrency);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static InvestmentValuationPrefillResult empty() {
        return new InvestmentValuationPrefillResult(List.of(), List.of(), List.of());
    }
}
