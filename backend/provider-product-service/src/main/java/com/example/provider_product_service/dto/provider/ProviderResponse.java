package com.example.provider_product_service.dto.provider;

import com.example.provider_product_service.model.provider.ProviderStatus;
import com.example.provider_product_service.model.provider.ProviderType;

import java.time.Instant;
import java.util.UUID;

public record ProviderResponse(
        UUID id,
        String providerCode,
        String name,
        ProviderType type,
        String registrationNumber,
        String contactEmail,
        String contactPhone,
        String website,
        ProviderStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
