package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.retirement.RetirementProjectionPointResponse;
import com.example.financial_engine_service.dto.retirement.RetirementProjectionResponse;
import com.example.financial_engine_service.dto.retirement.ScenarioSimulationResponse;
import com.example.financial_engine_service.entity.FinancialAnalysis;
import com.example.financial_engine_service.entity.FinancialProjection;
import com.example.financial_engine_service.enums.ProjectionPeriodType;
import com.example.financial_engine_service.enums.ScenarioType;
import com.example.financial_engine_service.repository.FinancialProjectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialProjectionPersistenceService {

    private final FinancialAnalysisQueryService analysisQueryService;
    private final com.example.financial_engine_service.repository.FinancialAnalysisRepository analysisRepository;
    private final FinancialProjectionRepository projectionRepository;

    public FinancialProjectionPersistenceService(
            FinancialAnalysisQueryService analysisQueryService,
            com.example.financial_engine_service.repository.FinancialAnalysisRepository analysisRepository,
            FinancialProjectionRepository projectionRepository) {
        this.analysisQueryService = analysisQueryService;
        this.analysisRepository = analysisRepository;
        this.projectionRepository = projectionRepository;
    }

    @Transactional
    public void saveRetirementProjection(Long userId, java.util.UUID analysisId,
                                         RetirementProjectionResponse response) {
        analysisQueryService.getMyAnalysis(userId, analysisId);
        FinancialAnalysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new com.example.financial_engine_service.exception.ResourceNotFoundException(
                        "Financial analysis not found"));

        projectionRepository.deleteAllByAnalysisId(analysisId);

        List<FinancialProjection> rows = response.yearlyProjection().stream()
                .map(point -> toEntity(analysis, point))
                .toList();

        projectionRepository.saveAll(rows);
    }

    @Transactional(readOnly = true)
    public List<FinancialProjection> getMyProjections(Long userId, java.util.UUID analysisId) {
        analysisQueryService.getMyAnalysis(userId, analysisId);
        return projectionRepository.findAllByAnalysisIdOrderByPeriodNumberAsc(analysisId);
    }

    private FinancialProjection toEntity(FinancialAnalysis analysis,
                                         RetirementProjectionPointResponse point) {
        FinancialProjection projection = new FinancialProjection();
        projection.setAnalysis(analysis);
        projection.setScenarioType(ScenarioType.EXPECTED);
        projection.setPeriodType(ProjectionPeriodType.YEARLY);
        projection.setPeriodNumber(point.year());
        projection.setProjectedIncome(point.projectedMonthlyRetirementIncome()
                .multiply(BigDecimal.valueOf(12)));
        projection.setProjectedExpenses(point.projectedAnnualExpenses());
        projection.setProjectedContributions(point.projectedContributions());
        projection.setProjectedCorpus(point.projectedCorpus());
        projection.setProjectedNetWorth(point.projectedCorpus());
        return projection;
    }

    @Transactional
    public void saveScenarios(Long userId, java.util.UUID analysisId,
                              List<ScenarioSimulationResponse> scenarios) {
        analysisQueryService.getMyAnalysis(userId, analysisId);
        FinancialAnalysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new com.example.financial_engine_service.exception.ResourceNotFoundException(
                        "Financial analysis not found"));

        List<FinancialProjection> rows = scenarios.stream().map(scenario -> {
            FinancialProjection projection = new FinancialProjection();
            projection.setAnalysis(analysis);
            projection.setScenarioType(scenario.scenarioType());
            projection.setPeriodType(ProjectionPeriodType.YEARLY);
            projection.setPeriodNumber(scenario.years());
            projection.setProjectedIncome(BigDecimal.ZERO);
            projection.setProjectedExpenses(scenario.projectedMonthlyExpenses()
                    .multiply(BigDecimal.valueOf(12)));
            projection.setProjectedContributions(BigDecimal.ZERO);
            projection.setProjectedCorpus(scenario.projectedCorpus());
            projection.setProjectedNetWorth(scenario.estimatedRealCorpus());
            return projection;
        }).toList();

        projectionRepository.saveAll(rows);
    }
}
