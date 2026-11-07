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
@Table(name = "customer_assets",
        indexes = @Index(
                name = "idx_asset_identity_user",
                columnList = "identity_user_id"
        )
)
public class Asset {
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
    private AssetCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate valuationDate;

    @Column(nullable = false)
    private boolean liquid;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;


    public Asset(
            String identityUserId,
            String name,
            AssetCategory category,
            BigDecimal currentValue,
            String currency,
            LocalDate valuationDate,
            boolean liquid,
            String notes
    ) {
        this.identityUserId = identityUserId;
        update(name, category, currentValue, currency, valuationDate, liquid, notes);
    }

    public void update(
            String name,
            AssetCategory category,
            BigDecimal currentValue,
            String currency,
            LocalDate valuationDate,
            boolean liquid,
            String notes
    ) {
        this.name = name;
        this.category = category;
        this.currentValue = currentValue;
        this.currency = currency;
        this.valuationDate = valuationDate;
        this.liquid = liquid;
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
