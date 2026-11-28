package com.example.payment_service.service;

import com.example.core.events.PaymentStatusEvent;
import com.example.payment_service.dto.MockPaymentOutcome;
import com.example.payment_service.dto.PaymentEventResponse;
import com.example.payment_service.dto.PaymentRequest;
import com.example.payment_service.dto.PaymentResponse;
import com.example.payment_service.kafka.PaymentStatusEventPublisher;
import com.example.payment_service.model.*;
import com.example.payment_service.repository.PaymentEventRepository;
import com.example.payment_service.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository eventRepository;
    private final PaymentStatusEventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentEventRepository eventRepository,
            PaymentStatusEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    private void publishPaymentStatusEvent(Payment payment) {

        // Preserve Goal Service events and publish all investment payment outcomes
        // for the Investment Service settlement consumer.
        if (payment.getBusinessType() != PaymentBusinessType.GOAL_CONTRIBUTION
                && !isInvestmentPayment(payment.getBusinessType())) {
            return;
        }

        PaymentStatusEvent event = new PaymentStatusEvent(
                UUID.randomUUID(),
                payment.getId(),
                payment.getIdentityUserId(),
                payment.getBusinessReference(),
                payment.getBusinessTransactionReference(),
                payment.getBusinessType().name(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                Instant.now()
        );

        Runnable publishAction = () -> eventPublisher.publish(event);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            publishAction.run();
                        }
                    }
            );
        } else {
            publishAction.run();
        }
    }

    public PaymentResponse createPayment(Long userId, PaymentRequest request) {
        Payment payment = new Payment();
        payment.setIdentityUserId(userId);
        payment.setPaymentReference("PAY-" + UUID.randomUUID());
        payment.setBusinessReference(request.businessReference().trim());
        payment.setBusinessType(request.businessType());
        String transactionReference = request.businessTransactionReference();
        if (isInvestmentPayment(request.businessType())) {
            validateUuidReference(request.businessReference(), "businessReference");
            if (transactionReference != null && !transactionReference.isBlank()) {
                validateUuidReference(transactionReference, "businessTransactionReference");
                payment.setBusinessTransactionReference(transactionReference.trim());
            } else if (request.businessType() != PaymentBusinessType.INVESTMENT_PURCHASE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "businessTransactionReference is required for additional contributions and redemptions");
            }
        } else if (transactionReference != null && !transactionReference.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Business transaction reference is supported only for investment payments");
        }
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.CREATED);

        Payment saved = paymentRepository.save(payment);
        recordEvent(saved, PaymentEventType.PAYMENT_CREATED,
                "PAYMENT_SERVICE", null, "Payment record created");
        return toResponse(saved);
    }

    public PaymentResponse processMockPayment(
            Long userId, UUID paymentId, MockPaymentOutcome outcome) {
        Payment payment = findOwnedPayment(userId, paymentId);

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only CREATED payments can be processed");
        }

        Instant now = Instant.now();
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setInitiatedAt(now);
        payment.setGatewayName("FINLONGEVITY_MOCK_GATEWAY");
        paymentRepository.save(payment);
        recordEvent(payment, PaymentEventType.PAYMENT_INITIATED,
                "MOCK_GATEWAY", null, "Mock payment processing started");

        payment.setGatewayTransactionId("MOCK-TXN-" + UUID.randomUUID());
        payment.setCompletedAt(Instant.now());

        if (outcome == MockPaymentOutcome.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setFailureReason(null);
            recordEvent(payment, PaymentEventType.PAYMENT_SUCCEEDED,
                    "MOCK_GATEWAY", payment.getGatewayTransactionId(),
                    "Mock payment marked successful; no real money was moved");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment failure");
            recordEvent(payment, PaymentEventType.PAYMENT_FAILED,
                    "MOCK_GATEWAY", payment.getGatewayTransactionId(),
                    "Mock payment marked failed; no real money was moved");
        }

        Payment savedPayment = paymentRepository.save(payment);
        if (savedPayment.getStatus() == PaymentStatus.SUCCESS || savedPayment.getStatus() == PaymentStatus.FAILED) {
            publishPaymentStatusEvent(savedPayment);
        }
        return toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(Long userId) {
        return paymentRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getMyPayment(Long userId, UUID paymentId) {
        return toResponse(findOwnedPayment(userId, paymentId));
    }

    @Transactional(readOnly = true)
    public List<PaymentEventResponse> getPaymentEvents(Long userId, UUID paymentId) {
        findOwnedPayment(userId, paymentId);
        return eventRepository.findAllByPaymentIdOrderByCreatedAtDesc(paymentId)
                .stream().map(this::toEventResponse).toList();
    }

    private boolean isInvestmentPayment(PaymentBusinessType businessType) {
        return businessType == PaymentBusinessType.INVESTMENT_PURCHASE
                || businessType == PaymentBusinessType.INVESTMENT_ADDITIONAL_CONTRIBUTION
                || businessType == PaymentBusinessType.INVESTMENT_REDEMPTION;
    }

    private void validateUuidReference(String value, String fieldName) {
        try {
            UUID.fromString(value.trim());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    fieldName + " must contain a valid UUID");
        }
    }

    private Payment findOwnedPayment(Long userId, UUID paymentId) {
        return paymentRepository.findByIdAndIdentityUserId(paymentId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private void recordEvent(Payment payment, PaymentEventType type,
                             String source, String externalEventId, String description) {
        PaymentEvent event = new PaymentEvent();
        event.setPayment(payment);
        event.setEventType(type);
        event.setSource(source);
        event.setExternalEventId(externalEventId);
        event.setDescription(description);
        eventRepository.save(event);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getIdentityUserId(), p.getPaymentReference(),
                p.getBusinessReference(), p.getBusinessType(), p.getAmount(),
                p.getCurrency(), p.getStatus(), p.getPaymentMethod(),
                p.getGatewayName(), p.getGatewayTransactionId(),
                p.getFailureReason(), p.getInitiatedAt(), p.getCompletedAt(),
                p.getCreatedAt(), p.getUpdatedAt(), p.getBusinessTransactionReference()
        );
    }

    private PaymentEventResponse toEventResponse(PaymentEvent e) {
        return new PaymentEventResponse(
                e.getId(), e.getPayment().getId(), e.getEventType(),
                e.getSource(), e.getExternalEventId(), e.getDescription(),
                e.getCreatedAt()
        );
    }
}
