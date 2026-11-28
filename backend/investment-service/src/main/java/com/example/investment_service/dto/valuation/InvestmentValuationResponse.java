package com.example.investment_service.dto.valuation;

import com.example.investment_service.model.valuation.InvestmentValuationSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentValuationResponse(
        UUID id,
        UUID investmentId,
        String productNameSnapshot,
        BigDecimal marketValue,
        String currency,
        LocalDate valuationDate,
        InvestmentValuationSource valuationSource,
        String sourceReference,
        Instant recordedAt
) {
}
