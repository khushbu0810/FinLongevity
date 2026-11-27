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
@Table(name = "insurance_applications",
        indexes = {
                @Index(name = "idx_ins_app_user", columnList = "identity_user_id"),
                @Index(name = "idx_ins_app_product", columnList = "provider_product_id"),
                @Index(name = "idx_ins_app_status", columnList = "status")
        })
public class InsuranceApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false)
    private Long identityUserId;

    // External ID owned by Provider & Product Service; not a JPA relationship.
    @Column(name = "provider_product_id", nullable = false)
    private UUID providerProductId;

    @Column(name = "product_name_snapshot", nullable = false, length = 180)
    private String productNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_type", nullable = false, length = 40)
    private InsuranceType insuranceType;

    @Column(name = "applicant_name", nullable = false, length = 150)
    private String applicantName;

    @Column(name = "applicant_date_of_birth", nullable = false)
    private LocalDate applicantDateOfBirth;

    @Column(name = "coverage_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InsuranceApplicationStatus status;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "decision_at")
    private Instant decisionAt;

    @Column(name = "decision_note", length = 1000)
    private String decisionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = InsuranceApplicationStatus.DRAFT;
        if (currency != null) currency = currency.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (currency != null) currency = currency.toUpperCase();
    }


}
