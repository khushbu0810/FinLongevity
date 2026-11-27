package com.example.insurance_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "insurance_policies",
       indexes = {
           @Index(name = "idx_ins_policy_user", columnList = "identity_user_id"),
           @Index(name = "idx_ins_policy_application", columnList = "application_id"),
           @Index(name = "idx_ins_policy_status", columnList = "status")
       })
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false)
    private Long identityUserId;

    // Application belongs to this service, so this is a local JPA relationship.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private InsuranceApplication application;

    @Column(name = "policy_number", nullable = false, unique = true, length = 80)
    private String policyNumber;

    @Column(name = "provider_product_id", nullable = false)
    private UUID providerProductId;

    @Column(name = "coverage_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "premium_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PremiumFrequency premiumFrequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InsurancePolicyStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_premium_due_date")
    private LocalDate nextPremiumDueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = InsurancePolicyStatus.ACTIVE;
        if (currency != null) currency = currency.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (currency != null) currency = currency.toUpperCase();
    }

}
