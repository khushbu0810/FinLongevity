package com.example.customer_service.model.risk_profile;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "customer_risk_profiles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_risk_profile_identity_user", columnNames = "identity_user_id")
    },
    indexes = {
        @Index(name = "idx_risk_profile_identity_user", columnList = "identity_user_id")
    }
)
public class CustomerRiskProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_user_id", nullable = false, length = 100, unique = true)
    private String identityUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tolerance", nullable = false, length = 30)
    private RiskTolerance riskTolerance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_experience", nullable = false, length = 30)
    private InvestmentExperience investmentExperience;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_horizon", nullable = false, length = 30)
    private InvestmentHorizon investmentHorizon;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "liquidity_requirement", nullable = false, length = 20)
    private LiquidityRequirement liquidityRequirement;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "customer_investment_preferences",
        joinColumns = @JoinColumn(name = "risk_profile_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "preference", nullable = false, length = 30)
    private Set<InvestmentPreference> investmentPreferences = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "customer_financial_objectives",
        joinColumns = @JoinColumn(name = "risk_profile_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "objective", nullable = false, length = 40)
    private Set<FinancialObjective> financialObjectives = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIdentityUserId() { return identityUserId; }
    public void setIdentityUserId(String identityUserId) { this.identityUserId = identityUserId; }

    public RiskTolerance getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(RiskTolerance riskTolerance) { this.riskTolerance = riskTolerance; }

    public InvestmentExperience getInvestmentExperience() { return investmentExperience; }
    public void setInvestmentExperience(InvestmentExperience investmentExperience) { this.investmentExperience = investmentExperience; }

    public InvestmentHorizon getInvestmentHorizon() { return investmentHorizon; }
    public void setInvestmentHorizon(InvestmentHorizon investmentHorizon) { this.investmentHorizon = investmentHorizon; }

    public LiquidityRequirement getLiquidityRequirement() { return liquidityRequirement; }
    public void setLiquidityRequirement(LiquidityRequirement liquidityRequirement) { this.liquidityRequirement = liquidityRequirement; }

    public Set<InvestmentPreference> getInvestmentPreferences() { return investmentPreferences; }
    public void setInvestmentPreferences(Set<InvestmentPreference> investmentPreferences) {
        this.investmentPreferences = investmentPreferences == null ? new HashSet<>() : new HashSet<>(investmentPreferences);
    }

    public Set<FinancialObjective> getFinancialObjectives() { return financialObjectives; }
    public void setFinancialObjectives(Set<FinancialObjective> financialObjectives) {
        this.financialObjectives = financialObjectives == null ? new HashSet<>() : new HashSet<>(financialObjectives);
    }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
