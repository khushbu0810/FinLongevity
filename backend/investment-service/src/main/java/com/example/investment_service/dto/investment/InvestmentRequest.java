package com.example.investment_service.dto.investment;

import com.example.investment_service.model.investment.InvestmentProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record InvestmentRequest(
        @NotNull
        UUID providerProductId,

        @NotBlank
        @Size(max = 180)
        String productNameSnapshot,

        @NotNull
        InvestmentProductType productType,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal investedAmount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @DecimalMin(value = "0.0000000001")
        BigDecimal units,

        UUID goalId
) {}
