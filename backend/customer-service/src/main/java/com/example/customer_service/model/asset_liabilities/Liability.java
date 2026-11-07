package com.example.customer_service.model.asset_liabilities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "customer_liabilities",
        indexes = @Index(
                name = "idx_liability_identity_user",
                columnList = "identity_user_id"
        )
)
public class Liability {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false,
            length = 100, updatable = false)
    private String identityUserId;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LiabilityCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(precision = 7, scale = 4)
    private BigDecimal annualInterestRate;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyEmi;

    private LocalDate nextPaymentDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Liability(
            String identityUserId,
            String name,
            LiabilityCategory category,
            BigDecimal outstandingAmount,
            String currency,
            BigDecimal annualInterestRate,
            BigDecimal monthlyEmi,
            LocalDate nextPaymentDate,
            String notes
    ) {
        this.identityUserId = identityUserId;
        update(name, category, outstandingAmount, currency,
                annualInterestRate, monthlyEmi, nextPaymentDate, notes);
    }

    public void update(
            String name,
            LiabilityCategory category,
            BigDecimal outstandingAmount,
            String currency,
            BigDecimal annualInterestRate,
            BigDecimal monthlyEmi,
            LocalDate nextPaymentDate,
            String notes
    ) {
        this.name = name;
        this.category = category;
        this.outstandingAmount = outstandingAmount;
        this.currency = currency;
        this.annualInterestRate = annualInterestRate;
        this.monthlyEmi = monthlyEmi;
        this.nextPaymentDate = nextPaymentDate;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

}
