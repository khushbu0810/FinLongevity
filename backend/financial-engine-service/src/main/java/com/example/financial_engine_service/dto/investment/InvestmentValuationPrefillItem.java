package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Reviewable valuation evidence for one investment; never automatically counted in retirement corpus. */
public record InvestmentValuationPrefillItem(
        UUID investmentId,
        String productName,
        String investmentStatus,
        String currency,
        BigDecimal netInvestedPrincipal,
        BigDecimal recordedMarketValue,
        BigDecimal usableMarketValue,
        BigDecimal unrealizedGainLoss,
        LocalDate valuationDate,
        String valuationSource,
        Long valuationAgeDays,
        String valuationStatus,
        boolean stale,
        boolean includedInCurrentRetirementCorpus,
        List<String> warnings
) {
    public InvestmentValuationPrefillItem {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
