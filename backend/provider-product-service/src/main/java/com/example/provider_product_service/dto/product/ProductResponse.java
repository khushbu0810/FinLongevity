package com.example.provider_product_service.dto.product;

import com.example.provider_product_service.model.product.ProductStatus;
import com.example.provider_product_service.model.product.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID providerId,
        String providerName,
        String externalProductCode,
        String name,
        String description,
        ProductType productType,
        ProductStatus status,
        String currency,
        BigDecimal minimumInvestment,
        BigDecimal maximumInvestment,
        boolean featured,
        Instant createdAt,
        Instant updatedAt
) {}
