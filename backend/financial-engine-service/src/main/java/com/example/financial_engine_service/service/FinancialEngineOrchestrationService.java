package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.*;
import com.example.financial_engine_service.engine.FinancialSnapshotCalculator;
import com.example.financial_engine_service.engine.LongevityScoreCalculator;
import com.example.financial_engine_service.entity.FinancialAnalysis;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancialEngineOrchestrationService {

    private final FinancialSnapshotCalculator snapshotCalculator;
    private final LongevityScoreCalculator longevityScoreCalculator;
    private final FinancialAnalysisPersistenceService persistenceService;
    private final FinancialAnalysisQueryService queryService;

    public FinancialEngineOrchestrationService(
            FinancialSnapshotCalculator snapshotCalculator,
            LongevityScoreCalculator longevityScoreCalculator,
            FinancialAnalysisPersistenceService persistenceService,
            FinancialAnalysisQueryService queryService) {
        this.snapshotCalculator = snapshotCalculator;
        this.longevityScoreCalculator = longevityScoreCalculator;
        this.persistenceService = persistenceService;
        this.queryService = queryService;
    }

    @Transactional
    public FinancialEngineAnalysisResponse analyze(
            Long userId, FinancialSnapshotRequest request) {

        FinancialSnapshotAnalysisResponse snapshot =
                snapshotCalculator.calculate(request);

        LongevityScoreResponse score = longevityScoreCalculator.calculate(
                request,
                snapshot.cashFlow(),
                snapshot.netWorth(),
                snapshot.emergencyFundMonths()
        );

        FinancialAnalysis saved = persistenceService.saveCompletedAnalysis(
                userId, snapshot, score);

        FinancialAnalysisResponse analysis =
                queryService.getMyAnalysis(userId, saved.getId());

        return new FinancialEngineAnalysisResponse(analysis, snapshot, score);
    }
}
