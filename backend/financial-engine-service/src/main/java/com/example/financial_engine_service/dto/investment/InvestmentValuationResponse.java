package com.example.financial_engine_service.dto.investment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Read model matching Investment Service's InvestmentValuationResponse contract. */
public record InvestmentValuationResponse(
        UUID id,
        UUID investmentId,
        String productNameSnapshot,
        BigDecimal marketValue,
        String currency,
        LocalDate valuationDate,
        String valuationSource,
        String sourceReference,
        Instant recordedAt
) {
}
