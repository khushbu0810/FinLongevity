package com.example.customer_service.repository;

import com.example.customer_service.model.income.EmploymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
public interface EmploymentDetailsRepository extends JpaRepository<EmploymentDetails, UUID> {
    Optional<EmploymentDetails> findByIdentityUserId(String identityUserId);
}
