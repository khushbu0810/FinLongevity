package com.example.customer_service.repository;

import com.example.customer_service.model.risk_profile.CustomerRiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RiskProfileRepository extends JpaRepository<CustomerRiskProfile, UUID> {

    Optional<CustomerRiskProfile> findByIdentityUserId(String identityUserId);

    boolean existsByIdentityUserId(String identityUserId);
}
