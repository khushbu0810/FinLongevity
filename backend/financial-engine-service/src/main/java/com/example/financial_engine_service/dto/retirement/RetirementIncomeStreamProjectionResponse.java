package com.example.financial_engine_service.dto.retirement;

import com.example.financial_engine_service.enums.RetirementIncomeFrequency;
import com.example.financial_engine_service.enums.RetirementIncomeType;

import java.math.BigDecimal;

public record RetirementIncomeStreamProjectionResponse(
        RetirementIncomeType type,
        String name,
        RetirementIncomeFrequency frequency,
        Integer startsAtAge,
        Integer endsAtAge,
        BigDecimal projectedMonthlyAmountAtRetirement,
        boolean activeAtRetirement
) {
    public BigDecimal projectedMonthlyIncomeAtRetirement() {
        return projectedMonthlyAmountAtRetirement;
    }
}
