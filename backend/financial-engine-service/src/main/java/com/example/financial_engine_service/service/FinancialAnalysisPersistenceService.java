package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.FinancialSnapshotAnalysisResponse;
import com.example.financial_engine_service.dto.LongevityScoreResponse;
import com.example.financial_engine_service.entity.FinancialAnalysis;
import com.example.financial_engine_service.enums.AnalysisStatus;
import com.example.financial_engine_service.enums.AnalysisType;
import com.example.financial_engine_service.repository.FinancialAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FinancialAnalysisPersistenceService {

    private final FinancialAnalysisRepository analysisRepository;

    public FinancialAnalysisPersistenceService(
            FinancialAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public FinancialAnalysis saveCompletedAnalysis(
            Long userId,
            FinancialSnapshotAnalysisResponse snapshot,
            LongevityScoreResponse score) {

        FinancialAnalysis analysis = new FinancialAnalysis();
        analysis.setUserId(userId);
        analysis.setAnalysisType(AnalysisType.FULL_ANALYSIS);
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setBaseCurrency(snapshot.currency());
        analysis.setAsOfDate(LocalDate.now());

        analysis.setMonthlyIncome(snapshot.cashFlow().monthlyIncome());
        analysis.setMonthlyExpenses(snapshot.cashFlow().monthlyExpenses());
        analysis.setMonthlyDebtPayments(snapshot.cashFlow().monthlyDebtPayments());
        analysis.setMonthlySurplus(snapshot.cashFlow().monthlySurplus());
        analysis.setSavingsRate(snapshot.cashFlow().savingsRatePercentage());

        analysis.setTotalAssets(snapshot.netWorth().totalAssets());
        analysis.setTotalLiabilities(snapshot.netWorth().totalLiabilities());
        analysis.setNetWorth(snapshot.netWorth().netWorth());

        analysis.setEmergencyFundMonths(snapshot.emergencyFundMonths());
        analysis.setFinancialRunwayMonths(snapshot.financialRunwayMonths());
        analysis.setLongevityScore(score.score());
        analysis.setHealthBand(score.healthBand());

        return analysisRepository.save(analysis);
    }
}
