package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.FinancialAnalysisResponse;
import com.example.financial_engine_service.entity.FinancialAnalysis;
import com.example.financial_engine_service.enums.AnalysisStatus;
import com.example.financial_engine_service.exception.ResourceNotFoundException;
import com.example.financial_engine_service.repository.FinancialAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FinancialAnalysisQueryService {

    private final FinancialAnalysisRepository analysisRepository;

    public FinancialAnalysisQueryService(FinancialAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public List<FinancialAnalysisResponse> getMyAnalyses(Long userId) {
        return analysisRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FinancialAnalysisResponse getMyAnalysis(Long userId, UUID analysisId) {
        FinancialAnalysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Financial analysis not found"));
        return toResponse(analysis);
    }

    public FinancialAnalysisResponse getLatestCompletedAnalysis(Long userId) {
        FinancialAnalysis analysis = analysisRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, AnalysisStatus.COMPLETED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No completed financial analysis found"));
        return toResponse(analysis);
    }

    private FinancialAnalysisResponse toResponse(FinancialAnalysis a) {
        return new FinancialAnalysisResponse(
                a.getId(), a.getUserId(), a.getAnalysisType(), a.getStatus(),
                a.getBaseCurrency(), a.getAsOfDate(), a.getMonthlyIncome(),
                a.getMonthlyExpenses(), a.getMonthlyDebtPayments(), a.getTotalAssets(),
                a.getTotalLiabilities(), a.getNetWorth(), a.getMonthlySurplus(),
                a.getSavingsRate(), a.getEmergencyFundMonths(), a.getFinancialRunwayMonths(),
                a.getLongevityScore(), a.getHealthBand(), a.getFailureReason(),
                a.getCreatedAt(), a.getUpdatedAt()
        );
    }
}
