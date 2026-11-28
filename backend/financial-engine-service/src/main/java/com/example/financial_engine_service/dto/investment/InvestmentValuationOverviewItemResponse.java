package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** One investment's principal evidence and latest recorded market valuation. */
public record InvestmentValuationOverviewItemResponse(
        UUID investmentId,
        String productNameSnapshot,
        String investmentStatus,
        String currency,
        BigDecimal netInvestedPrincipal,
        InvestmentValuationResponse latestValuation,
        String valuationStatus
) {

    public boolean hasValuation() {
        return latestValuation != null
                && "AVAILABLE".equalsIgnoreCase(valuationStatus);
    }

    /**
     * Computes valuation age without imposing a hidden freshness policy.
     * The caller supplies the maximum age appropriate to its use case.
     */
    public long valuationAgeDays(LocalDate asOfDate) {
        if (!hasValuation() || latestValuation.valuationDate() == null || asOfDate == null) {
            return -1L;
        }
        return Math.max(0L, ChronoUnit.DAYS.between(
                latestValuation.valuationDate(), asOfDate));
    }

    public boolean isValuationStale(LocalDate asOfDate, long maximumAgeDays) {
        if (maximumAgeDays < 0) {
            throw new IllegalArgumentException("Maximum valuation age cannot be negative");
        }
        long age = valuationAgeDays(asOfDate);
        return age >= 0L && age > maximumAgeDays;
    }
}
