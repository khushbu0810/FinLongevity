package com.example.goal_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "goal_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_goal_member_user", columnNames = {"goal_id", "user_id"}),
        indexes = @Index(name = "idx_goal_member_user", columnList = "user_id"))
public class GoalMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private FinancialGoal goal;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalMemberRole role;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) joinedAt = Instant.now();
        if (status == null) status = "ACTIVE";
    }

}
