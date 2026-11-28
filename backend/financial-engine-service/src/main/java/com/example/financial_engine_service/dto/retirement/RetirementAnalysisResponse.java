package com.example.financial_engine_service.dto.retirement;

import java.util.List;

public record RetirementAnalysisResponse(
        RetirementProjectionResponse retirementProjection,
        List<ScenarioSimulationResponse> scenarios
) {
}
