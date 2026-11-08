package com.example.provider_product_service.model.provider;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "providers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_provider_code", columnNames = "provider_code"),
                @UniqueConstraint(name = "uk_provider_registration_number", columnNames = "registration_number")
        }
)
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider_code", nullable = false, unique = true, length = 40)
    private String providerCode;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProviderType type;

    @Column(name = "registration_number", nullable = false, unique = true, length = 100)
    private String registrationNumber;

    @Column(nullable = false, length = 254)
    private String contactEmail;

    @Column(length = 20)
    private String contactPhone;

    @Column(length = 500)
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProviderStatus status = ProviderStatus.DRAFT;

    @Column(length = 500)
    private String description;

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
