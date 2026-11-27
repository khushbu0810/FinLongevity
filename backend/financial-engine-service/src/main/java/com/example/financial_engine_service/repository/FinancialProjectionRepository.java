package com.example.financial_engine_service.repository;

import com.example.financial_engine_service.entity.FinancialProjection;
import com.example.financial_engine_service.enums.ScenarioType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FinancialProjectionRepository
        extends JpaRepository<FinancialProjection, UUID> {

    List<FinancialProjection> findAllByAnalysisIdOrderByPeriodNumberAsc(UUID analysisId);

    List<FinancialProjection> findAllByAnalysisIdAndScenarioTypeOrderByPeriodNumberAsc(
            UUID analysisId, ScenarioType scenarioType);

    void deleteAllByAnalysisId(UUID analysisId);
}
