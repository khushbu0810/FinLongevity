package com.example.customer_service.model.income;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "income_sources", indexes = @Index(name = "idx_income_identity_user", columnList = "identity_user_id"))
public class IncomeSource {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false, length = 100, updatable = false)
    private String identityUserId;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private IncomeType incomeType;

    @Column(nullable = false, length = 120)
    private String sourceName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncomeFrequency frequency;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;


    public IncomeSource(String userId, IncomeType type, String name, BigDecimal amount,
                        IncomeFrequency frequency, String currency, boolean active) {
        this.identityUserId = userId;
        update(type, name, amount, frequency, currency, active);
    }

    public void update(IncomeType type, String name, BigDecimal amount,
                       IncomeFrequency frequency, String currency, boolean active) {
        this.incomeType = type;
        this.sourceName = name;
        this.amount = amount;
        this.frequency = frequency;
        this.currency = currency;
        this.active = active;
    }

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

}
