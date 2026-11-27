package com.example.goal_service.controller;

import com.example.goal_service.dto.*;
import com.example.goal_service.model.GoalStatus;
import com.example.goal_service.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialGoalResponse createGoal(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                            @Valid @RequestBody FinancialGoalRequest request) {
        return goalService.createGoal(userId, request);
    }

    @GetMapping
    public List<FinancialGoalResponse> getMyGoals(@RequestHeader("X-Authenticated-User-Id") Long userId) {
        return goalService.getMyGoals(userId);
    }

    @GetMapping("/{goalId}")
    public FinancialGoalResponse getMyGoal(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                           @PathVariable UUID goalId) {
        return goalService.getMyGoal(userId, goalId);
    }

    @PutMapping("/{goalId}")
    public FinancialGoalResponse updateGoal(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                            @PathVariable UUID goalId, @Valid @RequestBody FinancialGoalRequest request) {
        return goalService.updateGoal(userId, goalId, request);
    }

    @PatchMapping("/{goalId}/status")
    public FinancialGoalResponse changeGoalStatus(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                                  @PathVariable UUID goalId, @RequestParam GoalStatus status) {
        return goalService.changeGoalStatus(userId, goalId, status);
    }

    @PostMapping("/{goalId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public GoalMemberResponse addMember(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                        @PathVariable UUID goalId, @Valid @RequestBody GoalMemberRequest request) {
        return goalService.addMember(userId, goalId, request);
    }

    @GetMapping("/{goalId}/members")
    public List<GoalMemberResponse> getMembers(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                               @PathVariable UUID goalId) {
        return goalService.getMembers(userId, goalId);
    }

    @PostMapping("/{goalId}/contributions")
    @ResponseStatus(HttpStatus.CREATED)
    public GoalContributionResponse createContribution(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                                       @PathVariable UUID goalId, @Valid @RequestBody GoalContributionRequest request) {
        return goalService.createContribution(userId, goalId, request);
    }

    @GetMapping("/{goalId}/contributions")
    public List<GoalContributionResponse> getContributions(@RequestHeader("X-Authenticated-User-Id") Long userId,
                                                           @PathVariable UUID goalId) {
        return goalService.getContributions(userId, goalId);
    }
}
