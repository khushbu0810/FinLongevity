package com.example.investment_service.repository;

import com.example.investment_service.model.transaction.InvestmentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, UUID> {
    List<InvestmentTransaction> findAllByInvestmentIdAndIdentityUserIdOrderByCreatedAtDesc(
            UUID investmentId, UUID identityUserId);

    Optional<InvestmentTransaction> findByIdAndIdentityUserId(UUID id, UUID identityUserId);

    List<InvestmentTransaction> findAllByIdentityUserIdOrderByCreatedAtDesc(UUID identityUserId);
}
