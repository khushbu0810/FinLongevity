package com.example.core.events;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Shared event contract published by Payment Service and consumed by Goal Service.
 * Keep the fields and package identical in both services.
 */
@Getter
@Setter
public class PaymentStatusEvent {
    private UUID eventId;
    private UUID paymentId;
    private Long identityUserId;
    private String businessReference;
    private String businessType;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Instant occurredAt;

    public PaymentStatusEvent() {
    }

    public PaymentStatusEvent(UUID eventId, UUID paymentId, Long identityUserId,
                              String businessReference, String businessType,
                              String status, BigDecimal amount, String currency,
                              Instant occurredAt) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.identityUserId = identityUserId;
        this.businessReference = businessReference;
        this.businessType = businessType;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.occurredAt = occurredAt;
    }


}
