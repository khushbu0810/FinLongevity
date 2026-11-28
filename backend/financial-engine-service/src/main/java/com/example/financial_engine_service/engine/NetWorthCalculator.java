package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.FinancialSnapshotRequest;
import com.example.financial_engine_service.dto.NetWorthAnalysisResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class NetWorthCalculator {

    public NetWorthAnalysisResponse calculate(FinancialSnapshotRequest request) {
        BigDecimal assets = safe(request.totalAssets());
        BigDecimal liabilities = safe(request.totalLiabilities());
        BigDecimal netWorth = assets.subtract(liabilities)
                .setScale(2, RoundingMode.HALF_UP);

        return new NetWorthAnalysisResponse(
                assets.setScale(2, RoundingMode.HALF_UP),
                liabilities.setScale(2, RoundingMode.HALF_UP),
                netWorth,
                netWorth.signum() >= 0,
                request.currency()
        );
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
