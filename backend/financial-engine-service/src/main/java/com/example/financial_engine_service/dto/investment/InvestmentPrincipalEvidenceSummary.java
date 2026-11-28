package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;

/**
 * Currency-specific summary of transaction-backed historical principal.
 * This is evidence for manual review, not a current market valuation or corpus amount.
 */
public record InvestmentPrincipalEvidenceSummary(
        String currency,
        int eligibleInvestmentCount,
        BigDecimal totalSupportedNetPrincipal,
        boolean currentMarketValueAvailable,
        boolean includedInRetirementCorpus
) {
}
