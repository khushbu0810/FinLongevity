package com.example.customer_service.repository;

import com.example.customer_service.model.asset_liabilities.Liability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiabilityRepository extends JpaRepository<Liability, UUID> {

    List<Liability> findAllByIdentityUserIdOrderByCreatedAtDesc(String identityUserId);

    List<Liability> findAllByIdentityUserIdAndCurrency(String identityUserId, String currency);

    Optional<Liability> findByIdAndIdentityUserId(UUID id, String identityUserId);
}
