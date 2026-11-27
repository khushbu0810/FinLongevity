package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.FinancialAnalysisResponse;
import com.example.financial_engine_service.dto.FinancialDashboardResponse;
import com.example.financial_engine_service.dto.FinancialProjectionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FinancialDashboardService {

    private final FinancialAnalysisQueryService analysisQueryService;
    private final RetirementAndScenarioService retirementAndScenarioService;

    public FinancialDashboardService(
            FinancialAnalysisQueryService analysisQueryService,
            RetirementAndScenarioService retirementAndScenarioService) {
        this.analysisQueryService = analysisQueryService;
        this.retirementAndScenarioService = retirementAndScenarioService;
    }

    public FinancialDashboardResponse getMyDashboard(Long userId) {
        FinancialAnalysisResponse latestAnalysis =
                analysisQueryService.getLatestCompletedAnalysis(userId);

        List<FinancialProjectionResponse> projections =
                retirementAndScenarioService.getMyProjections(userId, latestAnalysis.id());

        return new FinancialDashboardResponse(latestAnalysis, projections);
    }
}
