package com.example.investment_service.repository;

import com.example.investment_service.model.transaction.InvestmentTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, UUID> {
    List<InvestmentTransaction> findAllByInvestmentIdAndIdentityUserIdOrderByCreatedAtDesc(
            UUID investmentId, Long identityUserId);

    Optional<InvestmentTransaction> findByIdAndIdentityUserId(UUID id, Long identityUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from InvestmentTransaction t where t.id = :transactionId and t.identityUserId = :userId")
    Optional<InvestmentTransaction> findByIdAndIdentityUserIdForUpdate(
            @Param("transactionId") UUID transactionId,
            @Param("userId") Long userId);

    boolean existsByPaymentReference(String paymentReference);

    Optional<InvestmentTransaction> findByPaymentReference(String paymentReference);

    List<InvestmentTransaction> findAllByIdentityUserIdOrderByCreatedAtDesc(Long identityUserId);
}
