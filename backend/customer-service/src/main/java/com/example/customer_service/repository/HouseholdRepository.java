package com.example.customer_service.repository;

import com.example.customer_service.model.household.Household;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HouseholdRepository extends JpaRepository<Household, UUID> {

    Optional<Household> findByOwnerIdentityUserId(String ownerIdentityUserId);

    Optional<Household> findByIdAndOwnerIdentityUserId(UUID id, String ownerIdentityUserId);

    boolean existsByOwnerIdentityUserId(String ownerIdentityUserId);
}
