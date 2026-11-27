package com.example.goal_service.repository;

import com.example.goal_service.model.GoalContribution;
import com.example.goal_service.model.GoalContributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, UUID> {
    List<GoalContribution> findAllByGoalIdOrderByCreatedAtDesc(UUID goalId);

    List<GoalContribution> findAllByContributorUserIdOrderByCreatedAtDesc(UUID contributorUserId);

    Optional<GoalContribution> findByIdAndContributorUserId(UUID id, UUID contributorUserId);

    Optional<GoalContribution> findByPaymentReference(String paymentReference);

    List<GoalContribution> findAllByGoalIdAndStatusOrderByCreatedAtDesc(UUID goalId, GoalContributionStatus status);
}
