package com.example.payment_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_user_created", columnList = "identity_user_id, created_at"),
        @Index(name = "idx_payment_business_reference", columnList = "business_reference")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false)
    private Long identityUserId;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 80)
    private String paymentReference;

    @Column(name = "business_reference", nullable = false, length = 120)
    private String businessReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 40)
    private PaymentBusinessType businessType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "gateway_name", length = 80)
    private String gatewayName;

    @Column(name = "gateway_transaction_id", length = 120)
    private String gatewayTransactionId;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "initiated_at")
    private Instant initiatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant n = Instant.now();
        createdAt = n;
        updatedAt = n;
        if (status == null) status = PaymentStatus.CREATED;
        if (currency == null || currency.isBlank()) currency = "INR";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
