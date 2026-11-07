package com.example.customer_service.dto.nominee;

import com.example.customer_service.model.nominee.NomineeRelationship;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record NomineeResponse(
        UUID id,
        String fullName,
        NomineeRelationship relationship,
        LocalDate dateOfBirth,
        String email,
        String phone,
        BigDecimal allocationPercentage,
        String guardianName,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
