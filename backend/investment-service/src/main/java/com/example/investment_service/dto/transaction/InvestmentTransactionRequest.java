package com.example.investment_service.dto.transaction;

import com.example.investment_service.model.transaction.InvestmentTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InvestmentTransactionRequest(
        @NotNull
        InvestmentTransactionType transactionType,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @Size(min = 3, max = 3)
        String currency,

        @DecimalMin("0.0000000001")
        BigDecimal units,

        @Size(max = 1000)
        String description
) {
}
