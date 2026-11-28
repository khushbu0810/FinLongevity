package com.example.investment_service.dto.valuation;

import com.example.investment_service.model.investment.InvestmentStatus;
import com.example.investment_service.model.valuation.InvestmentValuationStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record InvestmentValuationOverviewItemResponse(
        UUID investmentId,
        String productNameSnapshot,
        InvestmentStatus investmentStatus,
        String currency,
        BigDecimal netInvestedPrincipal,
        InvestmentValuationResponse latestValuation,
        InvestmentValuationStatus valuationStatus
) {
}
