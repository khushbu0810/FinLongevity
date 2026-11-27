package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.FinancialSnapshotRequest;
import com.example.financial_engine_service.dto.FinancialSnapshotAnalysisResponse;
import org.springframework.stereotype.Service;

@Service
public class FinancialSnapshotCalculator {

    private final CashFlowCalculator cashFlowCalculator;
    private final NetWorthCalculator netWorthCalculator;
    private final FinancialRunwayCalculator financialRunwayCalculator;

    public FinancialSnapshotCalculator(
            CashFlowCalculator cashFlowCalculator,
            NetWorthCalculator netWorthCalculator,
            FinancialRunwayCalculator financialRunwayCalculator) {
        this.cashFlowCalculator = cashFlowCalculator;
        this.netWorthCalculator = netWorthCalculator;
        this.financialRunwayCalculator = financialRunwayCalculator;
    }

    public FinancialSnapshotAnalysisResponse calculate(FinancialSnapshotRequest request) {
        return new FinancialSnapshotAnalysisResponse(
                cashFlowCalculator.calculate(request),
                netWorthCalculator.calculate(request),
                financialRunwayCalculator.calculateEmergencyFundMonths(request),
                financialRunwayCalculator.calculateFinancialRunwayMonths(request),
                request.currency()
        );
    }
}
