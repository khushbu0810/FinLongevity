package com.example.customer_service.repository;

import com.example.customer_service.model.household.HouseholdMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {

    List<HouseholdMember> findAllByHousehold_IdOrderByCreatedAtDesc(UUID householdId);

    Optional<HouseholdMember> findByIdAndHousehold_Id(UUID id, UUID householdId);

    Optional<HouseholdMember> findByHousehold_IdAndIdentityUserId(UUID householdId, String identityUserId);

    boolean existsByHousehold_IdAndIdentityUserId(UUID householdId, String identityUserId);
}
