package com.example.financial_engine_service.controller;

import com.example.financial_engine_service.dto.FinancialProjectionResponse;
import com.example.financial_engine_service.dto.retirement.RetirementAnalysisResponse;
import com.example.financial_engine_service.dto.retirement.RetirementProjectionRequest;
import com.example.financial_engine_service.service.RetirementAndScenarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/financial-engine/analyses/{analysisId}")
public class RetirementProjectionController {

    private final RetirementAndScenarioService retirementAndScenarioService;

    public RetirementProjectionController(RetirementAndScenarioService retirementAndScenarioService) {
        this.retirementAndScenarioService = retirementAndScenarioService;
    }

    @PostMapping("/retirement-projection")
    public RetirementAnalysisResponse createRetirementProjection(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID analysisId,
            @Valid @RequestBody RetirementProjectionRequest request) {
        return retirementAndScenarioService.createRetirementProjection(userId, analysisId, request);
    }

    @GetMapping("/projections")
    public List<FinancialProjectionResponse> getProjections(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID analysisId) {
        return retirementAndScenarioService.getMyProjections(userId, analysisId);
    }
}
