package com.example.investment_service.repository;

import com.example.investment_service.model.investment.Investment;
import com.example.investment_service.model.investment.InvestmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    List<Investment> findAllByIdentityUserIdOrderByCreatedAtDesc(UUID identityUserId);

    Optional<Investment> findByIdAndIdentityUserId(UUID id, UUID identityUserId);

    List<Investment> findAllByIdentityUserIdAndStatusOrderByCreatedAtDesc(
            UUID identityUserId, InvestmentStatus status);

    boolean existsByIdentityUserIdAndProviderProductIdAndStatus(
            UUID identityUserId, UUID providerProductId, InvestmentStatus status);
}
