package com.example.customer_service.repository;

import com.example.customer_service.model.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
    Optional<CustomerProfile> findByIdentityUserId(String identityUserId);
    boolean existsByIdentityUserId(String identityUserId);
}
