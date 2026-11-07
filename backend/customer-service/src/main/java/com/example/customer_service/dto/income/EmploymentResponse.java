package com.example.customer_service.dto.income;

import com.example.customer_service.model.income.EmploymentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EmploymentResponse(
        UUID id,
        EmploymentType employmentType,
        String employerName,
        String designation,
        LocalDate employmentStartDate,
        LocalDate employmentEndDate,
        boolean currentlyEmployed,
        Instant createdAt,
        Instant updatedAt) {
}
