package com.example.customer_service.dto.household;

import java.time.Instant;
import java.util.UUID;

public record HouseholdResponse(
        UUID id,
        String ownerIdentityUserId,
        String name,
        String description,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
}
