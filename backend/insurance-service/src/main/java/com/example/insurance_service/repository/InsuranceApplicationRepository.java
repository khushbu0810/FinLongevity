package com.example.insurance_service.repository;

import com.example.insurance_service.model.InsuranceApplication;
import com.example.insurance_service.model.InsuranceApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsuranceApplicationRepository extends JpaRepository<InsuranceApplication, UUID> {
    List<InsuranceApplication> findAllByIdentityUserIdOrderByCreatedAtDesc(Long identityUserId);

    Optional<InsuranceApplication> findByIdAndIdentityUserId(UUID id, Long identityUserId);

    List<InsuranceApplication> findAllByIdentityUserIdAndStatusOrderByCreatedAtDesc(
            UUID identityUserId, InsuranceApplicationStatus status);
}
