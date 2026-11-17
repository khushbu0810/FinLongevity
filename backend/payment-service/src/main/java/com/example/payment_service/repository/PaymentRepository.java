package com.example.payment_service.repository;

import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByIdentityUserIdOrderByCreatedAtDesc(UUID identityUserId);

    Optional<Payment> findByIdAndIdentityUserId(UUID id, UUID identityUserId);

    Optional<Payment> findByPaymentReferenceAndIdentityUserId(String paymentReference, UUID identityUserId);

    List<Payment> findAllByIdentityUserIdAndStatusOrderByCreatedAtDesc(
            UUID identityUserId, PaymentStatus status);

    boolean existsByPaymentReference(String paymentReference);
}
