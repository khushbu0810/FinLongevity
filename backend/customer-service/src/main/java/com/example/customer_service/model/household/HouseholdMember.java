package com.example.customer_service.model.household;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "customer_household_members",
        indexes = {
                @Index(name = "idx_household_member_household", columnList = "household_id"),
                @Index(name = "idx_household_member_identity_user", columnList = "identity_user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_household_linked_identity_user",
                        columnNames = {"household_id", "identity_user_id"}
                )
        }
)
public class HouseholdMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    // Null for a dependant who does not have a FinLongevity login.
    @Column(name = "identity_user_id", length = 120)
    private String identityUserId;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HouseholdRelationship relationship;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HouseholdRole role = HouseholdRole.MEMBER;

    private LocalDate dateOfBirth;

    @Column(length = 254)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }


}
