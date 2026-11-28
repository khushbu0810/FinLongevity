package com.example.financial_engine_service.dto.retirement;

import java.math.BigDecimal;

/** Cash-flow-based retirement corpus requirement and readiness summary. */
public record RetirementCorpusRequirementResponse(
        Integer retirementAge,
        Integer planningAge,
        BigDecimal requiredCorpusAtRetirement,
        BigDecimal projectedCorpusAtRetirement,
        BigDecimal corpusSurplus,
        BigDecimal corpusShortfall,
        BigDecimal fundingRatioPercentage,
        boolean projectedCorpusMeetsRequirement
) { }
