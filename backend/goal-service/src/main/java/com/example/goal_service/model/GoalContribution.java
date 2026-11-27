package com.example.goal_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "goal_contributions", indexes = {
        @Index(name = "idx_goal_contribution_goal", columnList = "goal_id"),
        @Index(name = "idx_goal_contribution_user", columnList = "contributor_user_id"),
        @Index(name = "idx_goal_contribution_status", columnList = "status")
})
public class GoalContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private FinancialGoal goal;

    @Column(name = "contributor_user_id", nullable = false)
    private Long contributorUserId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalContributionStatus status;

    @Column(name = "payment_reference", unique = true, length = 100)
    private String paymentReference;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = GoalContributionStatus.PENDING;
        if (currency != null) currency = currency.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (currency != null) currency = currency.toUpperCase();
    }

}
