package com.example.financial_engine_service.dto;

import com.example.financial_engine_service.enums.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialAnalysisResponse(
        UUID id,
        Long userId,
        AnalysisType analysisType,
        AnalysisStatus status,
        String baseCurrency,
        LocalDate asOfDate,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpenses,
        BigDecimal monthlyDebtPayments,
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal netWorth,
        BigDecimal monthlySurplus,
        BigDecimal savingsRate,
        BigDecimal emergencyFundMonths,
        BigDecimal financialRunwayMonths,
        Integer longevityScore,
        FinancialHealthBand healthBand,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {}
