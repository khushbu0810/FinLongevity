package com.example.investment_service.service;

import com.example.core.events.PaymentStatusEvent;
import com.example.investment_service.model.investment.Investment;
import com.example.investment_service.model.investment.InvestmentStatus;
import com.example.investment_service.model.transaction.InvestmentTransaction;
import com.example.investment_service.model.transaction.InvestmentTransactionStatus;
import com.example.investment_service.model.transaction.InvestmentTransactionType;
import com.example.investment_service.repository.InvestmentRepository;
import com.example.investment_service.repository.InvestmentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Applies terminal Payment Service events to the matching investment transaction. */
@Service
public class InvestmentPaymentSettlementService {

    private static final String PURCHASE = "INVESTMENT_PURCHASE";
    private static final String ADDITIONAL = "INVESTMENT_ADDITIONAL_CONTRIBUTION";
    private static final String REDEMPTION = "INVESTMENT_REDEMPTION";

    private final InvestmentRepository investmentRepository;
    private final InvestmentTransactionRepository transactionRepository;

    public InvestmentPaymentSettlementService(
            InvestmentRepository investmentRepository,
            InvestmentTransactionRepository transactionRepository) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void handlePaymentStatusEvent(PaymentStatusEvent event) {
        if (event == null || !isInvestmentBusinessType(event.getBusinessType())) {
            return;
        }

        String paymentStatus = normalize(event.getStatus());
        if (!"SUCCESS".equals(paymentStatus) && !"FAILED".equals(paymentStatus)) {
            return;
        }

        validateEvent(event);

        String paymentReference = event.getPaymentId().toString();
        Investment investment = investmentRepository
                .findByIdAndIdentityUserIdForUpdate(
                        parseUuid(event.getBusinessReference(), "businessReference"),
                        event.getIdentityUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Investment not found for payment event: " + event.getBusinessReference()));

        // Check the stable payment ID before resolving a pending transaction. This also
        // makes replay of an initial-purchase event idempotent when its transaction
        // reference was omitted by the older UI contract.
        var previouslySettled = transactionRepository.findByPaymentReference(paymentReference);
        if (previouslySettled.isPresent()) {
            InvestmentTransaction existing = previouslySettled.get();
            validateTransactionMatch(event, investment, existing);
            InvestmentTransactionStatus expectedStatus = "SUCCESS".equals(paymentStatus)
                    ? InvestmentTransactionStatus.SUCCESS
                    : InvestmentTransactionStatus.FAILED;
            if (existing.getStatus() != expectedStatus) {
                throw new IllegalStateException(
                        "A previously processed payment was replayed with a conflicting status");
            }
            return;
        }

        InvestmentTransaction transaction = resolveTransaction(event, investment);

        // Kafka is at-least-once. The payment ID is the stable idempotency key.
        if (paymentReference.equals(transaction.getPaymentReference())) {
            return;
        }
        if (transactionRepository.existsByPaymentReference(paymentReference)) {
            throw new IllegalArgumentException(
                    "Payment is already linked to a different investment transaction: " + paymentReference);
        }
        if (transaction.getPaymentReference() != null
                && !transaction.getPaymentReference().isBlank()) {
            throw new IllegalStateException(
                    "Investment transaction is already linked to another payment");
        }

        validateTransactionMatch(event, investment, transaction);
        validateLifecycle(investment, transaction);

        transaction.setPaymentReference(paymentReference);
        transaction.setProcessedAt(Instant.now());
        transaction.setStatus("SUCCESS".equals(paymentStatus)
                ? InvestmentTransactionStatus.SUCCESS
                : InvestmentTransactionStatus.FAILED);
        transaction.setDescription(appendDescription(
                transaction.getDescription(),
                "Payment " + paymentReference + " marked " + paymentStatus));
        transactionRepository.save(transaction);

        applyInvestmentTransition(investment, transaction, paymentStatus);
        investmentRepository.save(investment);
    }

    private InvestmentTransaction resolveTransaction(
            PaymentStatusEvent event, Investment investment) {
        String transactionReference = event.getBusinessTransactionReference();
        if (transactionReference != null && !transactionReference.isBlank()) {
            UUID transactionId = parseUuid(transactionReference, "businessTransactionReference");
            return transactionRepository
                    .findByIdAndIdentityUserIdForUpdate(transactionId, event.getIdentityUserId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Investment transaction not found: " + transactionReference));
        }

        if (!PURCHASE.equals(event.getBusinessType())) {
            throw new IllegalArgumentException(
                    "businessTransactionReference is required for additional contributions and redemptions");
        }

        // Initial purchase references remain optional for compatibility with the existing UI.
        List<InvestmentTransaction> pendingPurchases = transactionRepository
                .findAllByInvestmentIdAndIdentityUserIdOrderByCreatedAtDesc(
                        investment.getId(), event.getIdentityUserId())
                .stream()
                .filter(tx -> tx.getTransactionType() == InvestmentTransactionType.PURCHASE)
                .filter(tx -> tx.getStatus() == InvestmentTransactionStatus.INITIATED
                        || tx.getStatus() == InvestmentTransactionStatus.PENDING)
                .toList();

        if (pendingPurchases.size() != 1) {
            throw new IllegalArgumentException(
                    "Expected exactly one pending initial purchase transaction; found "
                            + pendingPurchases.size());
        }
        return pendingPurchases.getFirst();
    }

    private void validateEvent(PaymentStatusEvent event) {
        if (event.getPaymentId() == null) {
            throw new IllegalArgumentException("Payment event is missing paymentId");
        }
        if (event.getIdentityUserId() == null) {
            throw new IllegalArgumentException("Payment event is missing identityUserId");
        }
        parseUuid(event.getBusinessReference(), "businessReference");
        if (event.getAmount() == null || event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment event amount must be positive");
        }
        if (event.getCurrency() == null || !event.getCurrency().matches("(?i)[A-Z]{3}")) {
            throw new IllegalArgumentException("Payment event currency must be a 3-letter code");
        }
    }

    private void validateTransactionMatch(
            PaymentStatusEvent event,
            Investment investment,
            InvestmentTransaction transaction) {
        if (!Objects.equals(transaction.getInvestment().getId(), investment.getId())) {
            throw new IllegalArgumentException(
                    "Investment transaction does not belong to the referenced investment");
        }
        if (!Objects.equals(transaction.getIdentityUserId(), event.getIdentityUserId())
                || !Objects.equals(investment.getIdentityUserId(), event.getIdentityUserId())) {
            throw new IllegalArgumentException("Payment user does not match investment owner");
        }
        if (transaction.getAmount() == null
                || transaction.getAmount().compareTo(event.getAmount()) != 0) {
            throw new IllegalArgumentException("Payment amount does not match investment transaction amount");
        }
        if (transaction.getCurrency() == null
                || !transaction.getCurrency().equalsIgnoreCase(event.getCurrency())) {
            throw new IllegalArgumentException("Payment currency does not match investment transaction currency");
        }
        if (investment.getCurrency() == null
                || !investment.getCurrency().equalsIgnoreCase(event.getCurrency())) {
            throw new IllegalArgumentException("Payment currency does not match investment currency");
        }

        InvestmentTransactionType type = transaction.getTransactionType();
        boolean typeMatches = switch (event.getBusinessType()) {
            case PURCHASE -> type == InvestmentTransactionType.PURCHASE;
            case ADDITIONAL -> type == InvestmentTransactionType.ADDITIONAL_INVESTMENT;
            case REDEMPTION -> type == InvestmentTransactionType.PARTIAL_REDEMPTION
                    || type == InvestmentTransactionType.FULL_REDEMPTION;
            default -> false;
        };
        if (!typeMatches) {
            throw new IllegalArgumentException(
                    "Payment business type does not match investment transaction type");
        }
    }

    private void validateLifecycle(Investment investment, InvestmentTransaction transaction) {
        if (transaction.getStatus() != InvestmentTransactionStatus.INITIATED
                && transaction.getStatus() != InvestmentTransactionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only INITIATED or PENDING investment transactions can be settled");
        }

        InvestmentStatus status = investment.getStatus();
        switch (transaction.getTransactionType()) {
            case PURCHASE -> {
                if (status != InvestmentStatus.DRAFT && status != InvestmentStatus.PENDING_PAYMENT) {
                    throw new IllegalStateException(
                            "Initial purchase can only settle for a DRAFT or PENDING_PAYMENT investment");
                }
            }
            case ADDITIONAL_INVESTMENT -> {
                if (status != InvestmentStatus.ACTIVE
                        && status != InvestmentStatus.PARTIALLY_REDEEMED) {
                    throw new IllegalStateException(
                            "Additional contributions require an ACTIVE or PARTIALLY_REDEEMED investment");
                }
            }
            case PARTIAL_REDEMPTION, FULL_REDEMPTION -> {
                if (status != InvestmentStatus.ACTIVE
                        && status != InvestmentStatus.PARTIALLY_REDEEMED
                        && status != InvestmentStatus.REDEMPTION_PENDING) {
                    throw new IllegalStateException(
                            "Redemptions require an ACTIVE, PARTIALLY_REDEEMED or REDEMPTION_PENDING investment");
                }
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported investment transaction type for payment settlement: "
                            + transaction.getTransactionType());
        }
    }

    private void applyInvestmentTransition(
            Investment investment,
            InvestmentTransaction transaction,
            String paymentStatus) {
        if ("FAILED".equals(paymentStatus)) {
            if (transaction.getTransactionType() == InvestmentTransactionType.PURCHASE) {
                investment.setStatus(InvestmentStatus.FAILED);
            } else if ((transaction.getTransactionType() == InvestmentTransactionType.PARTIAL_REDEMPTION
                    || transaction.getTransactionType() == InvestmentTransactionType.FULL_REDEMPTION)
                    && investment.getStatus() == InvestmentStatus.REDEMPTION_PENDING) {
                investment.setStatus(hasSuccessfulPartialRedemption(investment)
                        ? InvestmentStatus.PARTIALLY_REDEEMED
                        : InvestmentStatus.ACTIVE);
            }
            return;
        }

        switch (transaction.getTransactionType()) {
            case PURCHASE -> {
                investment.setStatus(InvestmentStatus.ACTIVE);
                if (investment.getOpenedAt() == null) {
                    investment.setOpenedAt(Instant.now());
                }
            }
            case ADDITIONAL_INVESTMENT -> {
                // Keep ACTIVE/PARTIALLY_REDEEMED history; the transaction is the contribution ledger.
            }
            case PARTIAL_REDEMPTION -> investment.setStatus(InvestmentStatus.PARTIALLY_REDEEMED);
            case FULL_REDEMPTION -> investment.setStatus(InvestmentStatus.REDEEMED);
            default -> throw new IllegalArgumentException(
                    "Unsupported investment transaction type for successful payment");
        }
    }

    private boolean hasSuccessfulPartialRedemption(Investment investment) {
        return transactionRepository
                .findAllByInvestmentIdAndIdentityUserIdOrderByCreatedAtDesc(
                        investment.getId(), investment.getIdentityUserId())
                .stream()
                .anyMatch(tx -> tx.getTransactionType() == InvestmentTransactionType.PARTIAL_REDEMPTION
                        && tx.getStatus() == InvestmentTransactionStatus.SUCCESS);
    }

    private boolean isInvestmentBusinessType(String businessType) {
        return PURCHASE.equals(businessType)
                || ADDITIONAL.equals(businessType)
                || REDEMPTION.equals(businessType);
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(fieldName + " must contain a valid UUID", exception);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String appendDescription(String current, String addition) {
        if (current == null || current.isBlank()) return addition;
        return current + " | " + addition;
    }
}
