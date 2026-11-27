package com.example.financial_engine_service.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record CustomerFinancialDataResponse(
        JsonNode profile,
        JsonNode employment,
        JsonNode incomeSources,
        JsonNode assets,
        JsonNode liabilities,
        JsonNode activeExpenses,
        JsonNode riskProfile,
        JsonNode household
) {
}
