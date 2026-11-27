package com.example.goal_service.repository;

import com.example.goal_service.model.FinancialGoal;
import com.example.goal_service.model.GoalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {
    List<FinancialGoal> findAllByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    Optional<FinancialGoal> findByIdAndOwnerUserId(UUID id, Long ownerUserId);

    List<FinancialGoal> findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(Long ownerUserId, GoalStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT g FROM FinancialGoal g
        WHERE g.id = :goalId
        """)
    Optional<FinancialGoal> findByIdForUpdate(
            @Param("goalId") UUID goalId);

}
