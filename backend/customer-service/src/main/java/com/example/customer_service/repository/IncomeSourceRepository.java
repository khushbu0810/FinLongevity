package com.example.customer_service.repository;

import com.example.customer_service.model.income.IncomeSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface IncomeSourceRepository extends JpaRepository<IncomeSource, UUID> {
    List<IncomeSource> findAllByIdentityUserIdOrderByCreatedAtDesc(String identityUserId);
    Optional<IncomeSource> findByIdAndIdentityUserId(UUID id, String identityUserId);
}
