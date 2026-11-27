package com.example.goal_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GoalMemberRequest(
        @NotNull
        Long userId) {
}
