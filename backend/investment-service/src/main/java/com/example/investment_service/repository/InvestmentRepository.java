package com.example.investment_service.repository;

import com.example.investment_service.model.investment.Investment;
import com.example.investment_service.model.investment.InvestmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    List<Investment> findAllByIdentityUserIdOrderByCreatedAtDesc(Long identityUserId);

    Optional<Investment> findByIdAndIdentityUserId(UUID id, Long identityUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Investment i where i.id = :investmentId and i.identityUserId = :userId")
    Optional<Investment> findByIdAndIdentityUserIdForUpdate(
            @Param("investmentId") UUID investmentId,
            @Param("userId") Long userId);

    List<Investment> findAllByIdentityUserIdAndStatusOrderByCreatedAtDesc(
            Long identityUserId, InvestmentStatus status);

    boolean existsByIdentityUserIdAndProviderProductIdAndStatus(
            Long identityUserId, UUID providerProductId, InvestmentStatus status);
}
