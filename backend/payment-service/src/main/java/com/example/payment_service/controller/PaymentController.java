package com.example.payment_service.controller;

import com.example.payment_service.dto.*;
import com.example.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Must be injected by the trusted API Gateway after JWT validation.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Valid @RequestBody PaymentRequest request) {
        return paymentService.createPayment(userId, request);
    }

    @PostMapping("/{paymentId}/process-mock")
    public PaymentResponse processMockPayment(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Parameter(
                    name = "paymentId",
                    description = "Enter the PaymentId UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID paymentId,
            @RequestParam(name = "outcome", defaultValue = "SUCCESS") MockPaymentOutcome outcome) {
        return paymentService.processMockPayment(userId, paymentId, outcome);
    }

    @GetMapping
    public List<PaymentResponse> getMyPayments(
            @RequestHeader("X-Authenticated-User-Id") Long userId) {
        return paymentService.getMyPayments(userId);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getMyPayment(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Parameter(
                    name = "paymentId",
                    description = "Enter the PaymentId UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID paymentId) {
        return paymentService.getMyPayment(userId, paymentId);
    }

    @GetMapping("/{paymentId}/events")
    public List<PaymentEventResponse> getPaymentEvents(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Parameter(
                    name = "paymentId",
                    description = "Enter the PaymentId UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID paymentId) {
        return paymentService.getPaymentEvents(userId, paymentId);
    }
}
