package com.example.customer_service.model.income;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "employment_details",
       uniqueConstraints = @UniqueConstraint(name = "uk_employment_identity_user", columnNames = "identity_user_id"))
public class EmploymentDetails {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false, length = 100, updatable = false)
    private String identityUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmploymentType employmentType;

    @Column(length = 150)
    private String employerName;

    @Column(length = 120)
    private String designation;

    private LocalDate employmentStartDate;

    private LocalDate employmentEndDate;

    @Column(nullable = false)
    private boolean currentlyEmployed;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;


    public EmploymentDetails(String userId, EmploymentType type, String employer, String designation,
                             LocalDate start, LocalDate end, boolean current) {
        this.identityUserId = userId;
        update(type, employer, designation, start, end, current);
    }

    public void update(EmploymentType type, String employer, String designation,
                       LocalDate start, LocalDate end, boolean current) {
        this.employmentType = type;
        this.employerName = employer;
        this.designation = designation;
        this.employmentStartDate = start;
        this.employmentEndDate = end;
        this.currentlyEmployed = current;
    }

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

}
