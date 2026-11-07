package com.example.customer_service.dto.income;

import com.example.customer_service.model.income.EmploymentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmploymentRequest(
        @NotNull
        EmploymentType employmentType,

        @Size(max = 150)
        String employerName,

        @Size(max = 120)
        String designation,

        LocalDate employmentStartDate,

        LocalDate employmentEndDate,

        boolean currentlyEmployed
) {
    @AssertTrue(message = "Employment end date cannot be before start date")
    public boolean isDateRangeValid() {
        return employmentStartDate == null || employmentEndDate == null ||
                !employmentEndDate.isBefore(employmentStartDate);
    }

    @AssertTrue(message = "End date must be empty for current employment")
    public boolean isCurrentDateValid() {
        return !currentlyEmployed || employmentEndDate == null;
    }
}
