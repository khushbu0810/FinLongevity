package com.example.customer_service.dto.asset_liabilities;

import com.example.customer_service.model.asset_liabilities.AssetCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String name,
        AssetCategory category,
        BigDecimal currentValue,
        String currency,
        LocalDate valuationDate,
        boolean liquid,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
