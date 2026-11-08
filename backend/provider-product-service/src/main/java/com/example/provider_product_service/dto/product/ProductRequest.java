package com.example.provider_product_service.dto.product;

import com.example.provider_product_service.model.product.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Size(max = 60)
        String externalProductCode,

        @NotBlank
        @Size(max = 180)
        String name,

        @Size(max = 3000)
        String description,

        @NotNull
        ProductType productType,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal minimumInvestment,

        @DecimalMin("0.00")
        BigDecimal maximumInvestment,

        boolean featured
) {
}
