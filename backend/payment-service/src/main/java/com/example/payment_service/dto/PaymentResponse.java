package com.example.payment_service.dto;

import com.example.payment_service.model.PaymentBusinessType;
import com.example.payment_service.model.PaymentMethod;
import com.example.payment_service.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        Long identityUserId,
        String paymentReference,
        String businessReference,
        PaymentBusinessType businessType,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String gatewayName,
        String gatewayTransactionId,
        String failureReason,
        Instant initiatedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        String businessTransactionReference
) {}
