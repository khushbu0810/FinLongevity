package com.example.customer_service.repository;

import com.example.customer_service.model.expenses.CustomerExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerExpenseRepository extends JpaRepository<CustomerExpense, UUID> {

    List<CustomerExpense> findAllByIdentityUserIdOrderByCreatedAtDesc(String identityUserId);

    List<CustomerExpense> findAllByIdentityUserIdAndActiveOrderByCreatedAtDesc(
            String identityUserId, boolean active);

    List<CustomerExpense> findAllByIdentityUserIdAndCurrencyAndActive(
            String identityUserId, String currency, boolean active);

    Optional<CustomerExpense> findByIdAndIdentityUserId(UUID id, String identityUserId);
}
