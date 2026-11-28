package com.example.investment_service.model.valuation;

import com.example.investment_service.model.investment.Investment;
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
@Table(name = "investment_valuations", indexes = {
        @Index(name = "idx_inv_valuation_investment_date",
                columnList = "investment_id, valuation_date, created_at"),
        @Index(name = "idx_inv_valuation_user_date",
                columnList = "identity_user_id, valuation_date")
})
public class InvestmentValuation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;

    @Column(name = "identity_user_id", nullable = false)
    private Long identityUserId;

    @Column(name = "product_name_snapshot", nullable = false, length = 180)
    private String productNameSnapshot;

    @Column(name = "market_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "valuation_date", nullable = false)
    private LocalDate valuationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "valuation_source", nullable = false, length = 40)
    private InvestmentValuationSource valuationSource;

    @Column(name = "source_reference", length = 200)
    private String sourceReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
