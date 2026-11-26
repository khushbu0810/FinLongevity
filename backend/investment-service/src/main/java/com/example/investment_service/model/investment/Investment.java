package com.example.investment_service.model.investment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "investments", indexes = {
        @Index(name = "idx_investment_user_created", columnList = "identity_user_id, created_at"),
        @Index(name = "idx_investment_product", columnList = "provider_product_id")
})
public class Investment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false)
    private Long identityUserId;

    @Column(name = "provider_product_id", nullable = false)
    private UUID providerProductId;

    @Column(name = "product_name_snapshot", nullable = false, length = 180)
    private String productNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 40)
    private InvestmentProductType productType;

    @Column(name = "invested_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal investedAmount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(precision = 24, scale = 10)
    private BigDecimal units;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InvestmentStatus status = InvestmentStatus.DRAFT;

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant n = Instant.now();
        createdAt = n;
        updatedAt = n;
        if (status == null) status = InvestmentStatus.DRAFT;
        if (currency == null || currency.isBlank()) currency = "INR";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
