package com.example.payment_service.model;

public enum PaymentStatus {
    CREATED,
    INITIATED,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
