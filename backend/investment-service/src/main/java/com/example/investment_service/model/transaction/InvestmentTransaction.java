package com.example.investment_service.model.transaction;

import com.example.investment_service.model.investment.Investment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "investment_transactions", indexes = {
        @Index(name = "idx_inv_tx_investment_created", columnList = "investment_id, created_at"),
        @Index(name = "idx_inv_tx_user_created", columnList = "identity_user_id, created_at")
})
public class InvestmentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;

    @Column(name = "identity_user_id", nullable = false)
    private Long identityUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private InvestmentTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestmentTransactionStatus status = InvestmentTransactionStatus.INITIATED;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(precision = 24, scale = 10)
    private BigDecimal units;

    @Column(name = "payment_reference", length = 120)
    private String paymentReference;

    @Column(length = 1000)
    private String description;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant n = Instant.now();
        createdAt = n;
        updatedAt = n;
        if (status == null) status = InvestmentTransactionStatus.INITIATED;
        if (currency == null || currency.isBlank()) currency = "INR";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
