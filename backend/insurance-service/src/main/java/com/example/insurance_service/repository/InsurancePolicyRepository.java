package com.example.insurance_service.repository;

import com.example.insurance_service.model.InsurancePolicy;
import com.example.insurance_service.model.InsurancePolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, UUID> {
    List<InsurancePolicy> findAllByIdentityUserIdOrderByCreatedAtDesc(Long identityUserId);

    Optional<InsurancePolicy> findByIdAndIdentityUserId(UUID id, Long identityUserId);

    Optional<InsurancePolicy> findByApplicationIdAndIdentityUserId(UUID applicationId, Long identityUserId);

    List<InsurancePolicy> findAllByIdentityUserIdAndStatusOrderByCreatedAtDesc(
            UUID identityUserId, InsurancePolicyStatus status);
}
