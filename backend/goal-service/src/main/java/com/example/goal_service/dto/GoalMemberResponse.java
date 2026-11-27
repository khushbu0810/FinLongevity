package com.example.goal_service.dto;

import com.example.goal_service.model.GoalMemberRole;

import java.time.Instant;
import java.util.UUID;

public record GoalMemberResponse(
        UUID id,
        UUID goalId,
        Long userId,
        GoalMemberRole role,
        String status,
        Instant joinedAt) {
}
