package com.example.customer_service.dto.household;

import com.example.customer_service.model.household.HouseholdRelationship;
import com.example.customer_service.model.household.HouseholdRole;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HouseholdMemberResponse(
        UUID id,
        UUID householdId,
        String identityUserId,
        String fullName,
        HouseholdRelationship relationship,
        HouseholdRole role,
        LocalDate dateOfBirth,
        String email,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
}
