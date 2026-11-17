package com.example.payment_service.dto;

import com.example.payment_service.model.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank
        @Size(max = 120)
        String businessReference,

        @NotNull
        PaymentBusinessType businessType,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotNull
        PaymentMethod paymentMethod
) {
}
