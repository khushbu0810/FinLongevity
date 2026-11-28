package com.example.financial_engine_service.entity;

import com.example.financial_engine_service.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "financial_analyses",
    indexes = {
        @Index(name = "idx_analysis_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_analysis_status", columnList = "status")
    })
public class FinancialAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false, length = 40)
    private AnalysisType analysisType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status = AnalysisStatus.REQUESTED;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency = "INR";

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "monthly_income", precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "monthly_expenses", precision = 19, scale = 2)
    private BigDecimal monthlyExpenses;

    @Column(name = "monthly_debt_payments", precision = 19, scale = 2)
    private BigDecimal monthlyDebtPayments;

    @Column(name = "total_assets", precision = 19, scale = 2)
    private BigDecimal totalAssets;

    @Column(name = "total_liabilities", precision = 19, scale = 2)
    private BigDecimal totalLiabilities;

    @Column(name = "net_worth", precision = 19, scale = 2)
    private BigDecimal netWorth;

    @Column(name = "monthly_surplus", precision = 19, scale = 2)
    private BigDecimal monthlySurplus;

    @Column(name = "savings_rate", precision = 7, scale = 2)
    private BigDecimal savingsRate;

    @Column(name = "emergency_fund_months", precision = 9, scale = 2)
    private BigDecimal emergencyFundMonths;

    @Column(name = "financial_runway_months", precision = 9, scale = 2)
    private BigDecimal financialRunwayMonths;

    @Column(name = "longevity_score")
    private Integer longevityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_band", length = 30)
    private FinancialHealthBand healthBand;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (asOfDate == null) asOfDate = LocalDate.now();
        if (baseCurrency == null || baseCurrency.isBlank()) baseCurrency = "INR";
        if (status == null) status = AnalysisStatus.REQUESTED;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }


}
