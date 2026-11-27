
package com.example.goal_service.repository;

import com.example.goal_service.model.GoalContribution;
import com.example.goal_service.model.GoalContributionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalContributionRepository
        extends JpaRepository<GoalContribution, UUID> {

    List<GoalContribution> findAllByGoalIdOrderByCreatedAtDesc(UUID goalId);

    List<GoalContribution> findAllByContributorUserIdOrderByCreatedAtDesc(
            Long contributorUserId);

    Optional<GoalContribution> findByIdAndContributorUserId(
            UUID id, Long contributorUserId);

    Optional<GoalContribution> findByPaymentReference(String paymentReference);

    List<GoalContribution> findAllByGoalIdAndStatusOrderByCreatedAtDesc(
            UUID goalId, GoalContributionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c FROM GoalContribution c
            WHERE c.id = :contributionId
            """)
    Optional<GoalContribution> findByIdForUpdate(
            @Param("contributionId") UUID contributionId);
}
