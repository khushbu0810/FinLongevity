package com.example.investment_service.repository;

import com.example.investment_service.model.investment.Investment;
import com.example.investment_service.model.investment.InvestmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    List<Investment> findAllByIdentityUserIdOrderByCreatedAtDesc(Long identityUserId);

    Optional<Investment> findByIdAndIdentityUserId(UUID id, Long identityUserId);

    List<Investment> findAllByIdentityUserIdAndStatusOrderByCreatedAtDesc(
            Long identityUserId, InvestmentStatus status);

    boolean existsByIdentityUserIdAndProviderProductIdAndStatus(
            Long identityUserId, UUID providerProductId, InvestmentStatus status);
}
