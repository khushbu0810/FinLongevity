package com.example.goal_service.repository;

import com.example.goal_service.model.GoalMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalMemberRepository extends JpaRepository<GoalMember, UUID> {
    List<GoalMember> findAllByGoalIdOrderByJoinedAtAsc(UUID goalId);

    Optional<GoalMember> findByGoalIdAndUserId(UUID goalId, Long userId);

    boolean existsByGoalIdAndUserId(UUID goalId, Long userId);

    List<GoalMember> findAllByUserIdOrderByJoinedAtDesc(UUID userId);
}
