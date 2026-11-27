package com.example.financial_engine_service.entity;

import com.example.financial_engine_service.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "financial_projections",
    indexes = {
        @Index(name = "idx_projection_analysis", columnList = "analysis_id"),
        @Index(name = "idx_projection_analysis_period", columnList = "analysis_id, period_number")
    })
public class FinancialProjection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private FinancialAnalysis analysis;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_type", nullable = false, length = 20)
    private ScenarioType scenarioType;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private ProjectionPeriodType periodType = ProjectionPeriodType.YEARLY;

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @Column(name = "projected_income", precision = 19, scale = 2)
    private BigDecimal projectedIncome;

    @Column(name = "projected_expenses", precision = 19, scale = 2)
    private BigDecimal projectedExpenses;

    @Column(name = "projected_contributions", precision = 19, scale = 2)
    private BigDecimal projectedContributions;

    @Column(name = "projected_corpus", precision = 19, scale = 2)
    private BigDecimal projectedCorpus;

    @Column(name = "projected_net_worth", precision = 19, scale = 2)
    private BigDecimal projectedNetWorth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (periodType == null) periodType = ProjectionPeriodType.YEARLY;
    }

}
