package com.example.financial_engine_service.dto;

import java.math.BigDecimal;

public record FinancialSnapshotAnalysisResponse(
        CashFlowAnalysisResponse cashFlow,
        NetWorthAnalysisResponse netWorth,
        BigDecimal emergencyFundMonths,
        BigDecimal financialRunwayMonths,
        String currency
) {
}
