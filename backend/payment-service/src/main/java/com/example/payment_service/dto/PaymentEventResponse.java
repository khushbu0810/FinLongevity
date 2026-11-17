package com.example.payment_service.dto;

import com.example.payment_service.model.PaymentEventType;

import java.time.Instant;
import java.util.UUID;

public record PaymentEventResponse(
        UUID id,
        UUID paymentId,
        PaymentEventType eventType,
        String source,
        String externalEventId,
        String description,
        Instant createdAt
) {}
