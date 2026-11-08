package com.example.provider_product_service.model.product;

import com.example.provider_product_service.model.provider.Provider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "provider_products",
        indexes = {
                @Index(name = "idx_product_provider", columnList = "provider_id"),
                @Index(name = "idx_product_status_type", columnList = "status, product_type")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_provider_external_code",
                        columnNames = {"provider_id", "external_product_code"}
                )
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "external_product_code", nullable = false, length = 80)
    private String externalProductCode;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 40)
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(precision = 19, scale = 2)
    private BigDecimal minimumInvestment;

    @Column(precision = 19, scale = 2)
    private BigDecimal maximumInvestment;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (currency == null || currency.isBlank()) {
            currency = "INR";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }


}
