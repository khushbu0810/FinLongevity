package com.example.customer_service.dto.household;

import com.example.customer_service.model.household.HouseholdRelationship;
import com.example.customer_service.model.household.HouseholdRole;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record HouseholdMemberRequest(

        @NotBlank
        @Size(max = 120)
        String fullName,

        @NotNull
        HouseholdRelationship relationship,

        @NotNull
        HouseholdRole role,

        @Past
        LocalDate dateOfBirth,

        @Email
        @Size(max = 254)
        String email,

        @Pattern(regexp = "^[+]?[0-9() -]{7,20}$", message = "Phone number format is invalid")
        String phone,

        @Size(max = 120)
        String identityUserId
) {
}
