package com.example.customer_service.dto.risk_profile;

import com.example.customer_service.model.risk_profile.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RiskProfileRequest(

        @NotNull
        RiskTolerance riskTolerance,

        @NotNull
        InvestmentExperience investmentExperience,

        @NotNull
        InvestmentHorizon investmentHorizon,

        @NotNull
        LiquidityRequirement liquidityRequirement,

        @NotEmpty
        Set<InvestmentPreference> investmentPreferences,

        @NotEmpty
        Set<FinancialObjective> financialObjectives
) {
}
