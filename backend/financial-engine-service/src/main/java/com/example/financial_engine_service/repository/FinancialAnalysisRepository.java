package com.example.financial_engine_service.repository;

import com.example.financial_engine_service.entity.FinancialAnalysis;
import com.example.financial_engine_service.enums.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialAnalysisRepository extends JpaRepository<FinancialAnalysis, UUID> {

    List<FinancialAnalysis> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FinancialAnalysis> findByIdAndUserId(UUID id, Long userId);

    List<FinancialAnalysis> findAllByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, AnalysisStatus status);

    Optional<FinancialAnalysis> findFirstByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, AnalysisStatus status);
}
