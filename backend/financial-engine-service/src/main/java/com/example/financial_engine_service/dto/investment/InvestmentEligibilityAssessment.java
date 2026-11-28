package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Evidence-based assessment of one investment record.
 * The supported principal is not a current market valuation.
 */
public record InvestmentEligibilityAssessment(
        UUID investmentId,
        String productName,
        String investmentStatus,
        String currency,
        boolean eligibleForPrincipalEvidence,
        BigDecimal supportedNetPrincipal,
        int totalTransactionCount,
        int successfulTransactionCount,
        boolean currentMarketValueAvailable,
        List<String> reasons,
        List<String> warnings
) {
    public InvestmentEligibilityAssessment {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
