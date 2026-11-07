package com.example.customer_service.dto.risk_profile;

import com.example.customer_service.model.risk_profile.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RiskProfileResponse(
        UUID id,
        RiskTolerance riskTolerance,
        InvestmentExperience investmentExperience,
        InvestmentHorizon investmentHorizon,
        LiquidityRequirement liquidityRequirement,
        Set<InvestmentPreference> investmentPreferences,
        Set<FinancialObjective> financialObjectives,
        Instant createdAt,
        Instant updatedAt
) {
}
