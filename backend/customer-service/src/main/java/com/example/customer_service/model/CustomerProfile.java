package com.example.customer_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "customer_profiles", uniqueConstraints = @UniqueConstraint(name = "uk_customer_identity_user", columnNames = "identity_user_id"))
public class CustomerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false, length = 100, updatable = false)
    private String identityUserId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 20)
    private String phone;

    private LocalDate dateOfBirth;

    @Column(length = 30)
    private String gender;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;


    public CustomerProfile(String identityUserId, String firstName, String lastName, String email, String phone, LocalDate dateOfBirth, String gender) {
        this.identityUserId = identityUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
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

    public void update(String firstName, String lastName, String email, String phone, LocalDate dateOfBirth, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }


}
