package com.example.customer_service.dto.profile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerProfileResponse(
        UUID customerId,
        String identityUserId,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String gender,
        Instant createdAt,
        Instant updatedAt
) {
}
