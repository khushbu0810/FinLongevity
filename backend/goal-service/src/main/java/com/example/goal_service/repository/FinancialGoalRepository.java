package com.example.goal_service.repository;

import com.example.goal_service.model.FinancialGoal;
import com.example.goal_service.model.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {
    List<FinancialGoal> findAllByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    Optional<FinancialGoal> findByIdAndOwnerUserId(UUID id, Long ownerUserId);

    List<FinancialGoal> findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(Long ownerUserId, GoalStatus status);
}
