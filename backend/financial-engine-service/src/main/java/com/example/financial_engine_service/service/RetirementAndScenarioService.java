package com.example.financial_engine_service.service;


import com.example.financial_engine_service.dto.FinancialProjectionResponse;
import com.example.financial_engine_service.dto.retirement.*;
import com.example.financial_engine_service.dto.retirement.ScenarioSimulationResponse;
import com.example.financial_engine_service.engine.*;
import com.example.financial_engine_service.entity.FinancialAnalysis;
import com.example.financial_engine_service.repository.FinancialAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RetirementAndScenarioService {

    private final RetirementProjectionCalculator retirementCalculator;
    private final ScenarioSimulationCalculator scenarioCalculator;
    private final FinancialAnalysisRepository analysisRepository;
    private final FinancialAnalysisQueryService analysisQueryService;
    private final FinancialProjectionPersistenceService projectionPersistenceService;

    public RetirementAndScenarioService(
            RetirementProjectionCalculator retirementCalculator,
            ScenarioSimulationCalculator scenarioCalculator,
            FinancialAnalysisRepository analysisRepository,
            FinancialAnalysisQueryService analysisQueryService,
            FinancialProjectionPersistenceService projectionPersistenceService) {
        this.retirementCalculator = retirementCalculator;
        this.scenarioCalculator = scenarioCalculator;
        this.analysisRepository = analysisRepository;
        this.analysisQueryService = analysisQueryService;
        this.projectionPersistenceService = projectionPersistenceService;
    }

    @Transactional
    public com.example.financial_engine_service.dto.retirement.RetirementAnalysisResponse createRetirementProjection(
            Long userId, UUID analysisId, RetirementProjectionRequest request) {
        FinancialAnalysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new com.example.financial_engine_service.exception.ResourceNotFoundException(
                        "Financial analysis not found"));

        if (request.retirementAge() <= request.currentAge()) {
            throw new IllegalArgumentException("Retirement age must be greater than current age");
        }
        int yearsUntilRetirement = request.retirementAge() - request.currentAge();
        if (!request.yearsToProject().equals(yearsUntilRetirement)) {
            throw new IllegalArgumentException(
                    "yearsToProject must equal retirementAge - currentAge (" + yearsUntilRetirement + ")");
        }

        RetirementProjectionResponse projection =
                retirementCalculator.calculate(request, analysis.getBaseCurrency());

        ScenarioSimulationRequest expectedScenarioRequest = new ScenarioSimulationRequest(
                request.currentInvestmentCorpus(),
                request.monthlyContribution(),
                request.currentMonthlyRetirementExpenses(),
                request.annualExpectedReturnPercentage(),
                request.annualInflationPercentage(),
                request.yearsToProject()
        );
        List<ScenarioSimulationResponse> scenarios = scenarioCalculator.simulate(expectedScenarioRequest);

        projectionPersistenceService.saveRetirementProjection(userId, analysisId, projection);
        projectionPersistenceService.saveScenarios(userId, analysisId, scenarios);

        return new RetirementAnalysisResponse(projection, scenarios);
    }

    @Transactional(readOnly = true)
    public List<FinancialProjectionResponse>
    getMyProjections(Long userId, UUID analysisId) {
        analysisQueryService.getMyAnalysis(userId, analysisId);
        return projectionPersistenceService.getMyProjections(userId, analysisId)
                .stream()
                .map(p -> new FinancialProjectionResponse(
                        p.getId(), p.getAnalysis().getId(), p.getScenarioType(),
                        p.getPeriodType(), p.getPeriodNumber(), p.getProjectedIncome(),
                        p.getProjectedExpenses(), p.getProjectedContributions(),
                        p.getProjectedCorpus(), p.getProjectedNetWorth()))
                .toList();
    }
}
