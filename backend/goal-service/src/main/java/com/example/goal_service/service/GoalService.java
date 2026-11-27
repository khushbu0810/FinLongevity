package com.example.goal_service.service;

import com.example.core.events.PaymentStatusEvent;
import com.example.goal_service.client.PaymentServiceClient;
import com.example.goal_service.dto.*;
import com.example.goal_service.model.*;
import com.example.goal_service.repository.FinancialGoalRepository;
import com.example.goal_service.repository.GoalContributionRepository;
import com.example.goal_service.repository.GoalMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class GoalService {
    private final FinancialGoalRepository goalRepository;
    private final GoalMemberRepository memberRepository;
    private final GoalContributionRepository contributionRepository;
    private final PaymentServiceClient paymentServiceClient;

    public GoalService(
            FinancialGoalRepository goalRepository,
            GoalMemberRepository memberRepository,
            GoalContributionRepository contributionRepository,
            PaymentServiceClient paymentServiceClient) {

        this.goalRepository = goalRepository;
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.paymentServiceClient = paymentServiceClient;
    }


    @Transactional
    public void handlePaymentStatusEvent(PaymentStatusEvent event) {
        if (!"GOAL_CONTRIBUTION".equals(event.getBusinessType())) {
            return;
        }
        UUID contributionId;
        try {
            contributionId = UUID.fromString(event.getBusinessReference());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid goal contribution reference: " + event.getBusinessReference());
        }

        GoalContribution contribution = contributionRepository
                .findByIdForUpdate(contributionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Goal contribution not found: " + contributionId));

        if (contribution.getStatus() != GoalContributionStatus.PENDING) {
            return;
        }

        if (!contribution.getContributorUserId().equals(event.getIdentityUserId())) {
            throw new IllegalArgumentException("Payment user does not match contribution owner");
        }

        if (contribution.getAmount().compareTo(event.getAmount()) != 0) {
            throw new IllegalArgumentException("Payment amount does not match contribution amount");
        }

        if (!contribution.getCurrency().equalsIgnoreCase(event.getCurrency())) {
            throw new IllegalArgumentException("Payment currency does not match contribution currency");
        }

        String paymentStatus = event.getStatus()
                .toUpperCase(Locale.ROOT);

        if (!paymentStatus.equals("SUCCESS") && !paymentStatus.equals("FAILED")) {
            return;
        }

        FinancialGoal goal = goalRepository.findByIdForUpdate(
                        contribution.getGoal().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Goal not found for contribution"));

        contribution.setPaymentReference(
                event.getPaymentId().toString());

        if (paymentStatus.equals("SUCCESS")) {
            contribution.setStatus(GoalContributionStatus.CONFIRMED);
            goal.setCurrentAmount(goal.getCurrentAmount().add(contribution.getAmount()));
            goalRepository.save(goal);
        } else {
            contribution.setStatus(GoalContributionStatus.FAILED);
        }
        contributionRepository.save(contribution);
    }


    public FinancialGoalResponse createGoal(Long userId, FinancialGoalRequest request) {
        FinancialGoal goal = new FinancialGoal();
        goal.setOwnerUserId(userId);
        applyRequest(goal, request);
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setStatus(GoalStatus.ACTIVE);
        FinancialGoal saved = goalRepository.save(goal);
        if (saved.getGoalType() == GoalType.GROUP) {
            GoalMember owner = new GoalMember();
            owner.setGoal(saved);
            owner.setUserId(userId);
            owner.setRole(GoalMemberRole.OWNER);
            owner.setStatus("ACTIVE");
            memberRepository.save(owner);
        }
        return toGoalResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FinancialGoalResponse> getMyGoals(Long userId) {
        return goalRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toGoalResponse).toList();
    }

    @Transactional(readOnly = true)
    public FinancialGoalResponse getMyGoal(Long userId, UUID goalId) {
        return toGoalResponse(findOwnedGoal(userId, goalId));
    }

    public FinancialGoalResponse updateGoal(Long userId, UUID goalId, FinancialGoalRequest request) {
        FinancialGoal goal = findOwnedGoal(userId, goalId);
        if (goal.getStatus() == GoalStatus.COMPLETED || goal.getStatus() == GoalStatus.CANCELLED)
            throw new IllegalStateException("Completed or cancelled goals cannot be updated");
        if (goal.getGoalType() != request.goalType())
            throw new IllegalArgumentException("Goal type cannot be changed after creation");
        applyRequest(goal, request);
        return toGoalResponse(goalRepository.save(goal));
    }

    public FinancialGoalResponse changeGoalStatus(Long userId, UUID goalId, GoalStatus status) {
        FinancialGoal goal = findOwnedGoal(userId, goalId);
        if (status == GoalStatus.ACTIVE || status == GoalStatus.PAUSED || status == GoalStatus.CANCELLED) {
            if (goal.getStatus() == GoalStatus.COMPLETED || goal.getStatus() == GoalStatus.CANCELLED)
                throw new IllegalStateException("Completed or cancelled goals are terminal");
            goal.setStatus(status);
        } else if (status == GoalStatus.COMPLETED) {
            if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0)
                throw new IllegalStateException("Goal cannot be completed before reaching its target");
            goal.setStatus(status);
        } else throw new IllegalArgumentException("Unsupported goal status");
        return toGoalResponse(goalRepository.save(goal));
    }

    public GoalMemberResponse addMember(Long ownerUserId, UUID goalId, GoalMemberRequest request) {
        FinancialGoal goal = findOwnedGoal(ownerUserId, goalId);
        if (goal.getGoalType() != GoalType.GROUP)
            throw new IllegalStateException("Members can only be added to group goals");
        if (goal.getStatus() != GoalStatus.ACTIVE)
            throw new IllegalStateException("Members can only be added to active goals");
        if (memberRepository.existsByGoalIdAndUserId(goalId, request.userId()))
            throw new IllegalArgumentException("User is already a member of this goal");
        GoalMember member = new GoalMember();
        member.setGoal(goal);
        member.setUserId(request.userId());
        member.setRole(GoalMemberRole.CONTRIBUTOR);
        member.setStatus("ACTIVE");
        return toMemberResponse(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<GoalMemberResponse> getMembers(Long userId, UUID goalId) {
        FinancialGoal goal = findGoalAccessibleToUser(userId, goalId);
        if (goal.getGoalType() != GoalType.GROUP) throw new IllegalStateException("This is not a group goal");
        return memberRepository.findAllByGoalIdOrderByJoinedAtAsc(goalId)
                .stream().map(this::toMemberResponse).toList();
    }

    public GoalContributionResponse createContribution(Long userId, UUID goalId, GoalContributionRequest request) {
        FinancialGoal goal = findGoalAccessibleToUser(userId, goalId);
        if (goal.getStatus() != GoalStatus.ACTIVE)
            throw new IllegalStateException("Contributions are allowed only for active goals");
        if (!goal.getCurrency().equalsIgnoreCase(request.currency()))
            throw new IllegalArgumentException("Contribution currency must match the goal currency");
        GoalContribution contribution = new GoalContribution();
        contribution.setGoal(goal);
        contribution.setContributorUserId(userId);
        contribution.setAmount(request.amount());
        contribution.setCurrency(request.currency().toUpperCase());
        contribution.setNote(request.note());
        contribution.setStatus(GoalContributionStatus.PENDING);

        // Save first so we have a contribution UUID for Payment Service.
        GoalContribution savedContribution = contributionRepository.saveAndFlush(contribution);

        // Create corresponding payment.
        PaymentServiceClient.PaymentCreationResponse payment =
                paymentServiceClient.createGoalContributionPayment(
                        userId,
                        savedContribution.getId(),
                        savedContribution.getAmount(),
                        savedContribution.getCurrency()
                );

        if (payment == null || payment.id() == null) {
            throw new IllegalStateException("Payment Service did not return a payment ID");
        }

        savedContribution.setPaymentReference(payment.id().toString());

        savedContribution = contributionRepository.save(savedContribution);

        return toContributionResponse(savedContribution);
    }


    @Transactional(readOnly = true)
    public List<GoalContributionResponse> getContributions(Long userId, UUID goalId) {
        findGoalAccessibleToUser(userId, goalId);
        return contributionRepository.findAllByGoalIdOrderByCreatedAtDesc(goalId)
                .stream().map(this::toContributionResponse).toList();
    }

    private void applyRequest(FinancialGoal goal, FinancialGoalRequest request) {
        goal.setName(request.name().trim());
        goal.setDescription(request.description());
        goal.setGoalType(request.goalType());
        goal.setTargetAmount(request.targetAmount());
        goal.setCurrency(request.currency().trim().toUpperCase());
        goal.setTargetDate(request.targetDate());
    }

    private FinancialGoal findOwnedGoal(Long userId, UUID goalId) {
        return goalRepository.findByIdAndOwnerUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
    }

    private FinancialGoal findGoalAccessibleToUser(Long userId, UUID goalId) {
        FinancialGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        if (goal.getOwnerUserId().equals(userId)) return goal;
        boolean activeMember = memberRepository.findByGoalIdAndUserId(goalId, userId)
                .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus())).isPresent();
        if (!activeMember) throw new IllegalArgumentException("Goal not found");
        return goal;
    }


    public GroupGoalAnalyticsResponse getGroupGoalAnalytics(
            Long userId, UUID goalId) {

        FinancialGoal goal = findGoalAccessibleToUser(userId, goalId);

        if (goal.getGoalType() != GoalType.GROUP) {
            throw new IllegalStateException(
                    "Analytics are available only for group goals");
        }

        List<GoalContribution> contributions =
                contributionRepository.findAllByGoalIdOrderByCreatedAtDesc(goalId);

        List<GoalMember> members =
                memberRepository.findAllByGoalIdOrderByJoinedAtAsc(goalId);

        long confirmedCount = contributions.stream()
                .filter(c -> c.getStatus() == GoalContributionStatus.CONFIRMED)
                .count();

        long pendingCount = contributions.stream()
                .filter(c -> c.getStatus() == GoalContributionStatus.PENDING)
                .count();

        long failedCount = contributions.stream()
                .filter(c -> c.getStatus() == GoalContributionStatus.FAILED)
                .count();

        List<GroupMemberContributionAnalyticsResponse> memberAnalytics =
                members.stream()
                        .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus()))
                        .map(member -> {

                            List<GoalContribution> memberContributions =
                                    contributions.stream()
                                            .filter(c -> c.getContributorUserId()
                                                    .equals(member.getUserId()))
                                            .toList();

                            BigDecimal confirmedAmount = memberContributions.stream()
                                    .filter(c -> c.getStatus()
                                            == GoalContributionStatus.CONFIRMED)
                                    .map(GoalContribution::getAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal pendingAmount = memberContributions.stream()
                                    .filter(c -> c.getStatus()
                                            == GoalContributionStatus.PENDING)
                                    .map(GoalContribution::getAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            long successfulCount = memberContributions.stream()
                                    .filter(c -> c.getStatus()
                                            == GoalContributionStatus.CONFIRMED)
                                    .count();

                            long memberFailedCount = memberContributions.stream()
                                    .filter(c -> c.getStatus()
                                            == GoalContributionStatus.FAILED)
                                    .count();

                            BigDecimal totalAmount =
                                    confirmedAmount.add(pendingAmount);

                            BigDecimal percentage = BigDecimal.ZERO;

                            if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0) {
                                percentage = confirmedAmount
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(
                                                goal.getCurrentAmount(),
                                                2,
                                                RoundingMode.HALF_UP
                                        );
                            }

                            return new GroupMemberContributionAnalyticsResponse(
                                    member.getUserId(),
                                    totalAmount,
                                    confirmedAmount,
                                    pendingAmount,
                                    successfulCount,
                                    memberFailedCount,
                                    percentage
                            );
                        })
                        .toList();

        BigDecimal remainingAmount = goal.getTargetAmount()
                .subtract(goal.getCurrentAmount())
                .max(BigDecimal.ZERO);

        BigDecimal progressPercentage = BigDecimal.ZERO;

        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = goal.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            goal.getTargetAmount(),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return new GroupGoalAnalyticsResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                remainingAmount,
                progressPercentage,
                goal.getCurrency(),
                contributions.size(),
                confirmedCount,
                pendingCount,
                failedCount,
                memberAnalytics
        );
    }



    private FinancialGoalResponse toGoalResponse(FinancialGoal g) {
        BigDecimal remainingAmount = g.getTargetAmount()
                .subtract(g.getCurrentAmount())
                .max(BigDecimal.ZERO);
        BigDecimal progressPercentage = g.getCurrentAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        g.getTargetAmount(),
                        2,
                        java.math.RoundingMode.HALF_UP
                );
        return new FinancialGoalResponse(g.getId(),
                g.getName(), g.getDescription(), g.getGoalType(), g.getTargetAmount(),
                g.getCurrentAmount(), remainingAmount, progressPercentage, g.getCurrency(), g.getTargetDate(),
                g.getStatus(), g.getCreatedAt(), g.getUpdatedAt()
        );
    }

    private GoalMemberResponse toMemberResponse(GoalMember m) {
        return new GoalMemberResponse(m.getId(), m.getGoal().getId(), m.getUserId(),
                m.getRole(), m.getStatus(), m.getJoinedAt());
    }

    private GoalContributionResponse toContributionResponse(GoalContribution c) {
        return new GoalContributionResponse(c.getId(), c.getGoal().getId(), c.getContributorUserId(),
                c.getAmount(), c.getCurrency(), c.getStatus(), c.getPaymentReference(),
                c.getNote(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
