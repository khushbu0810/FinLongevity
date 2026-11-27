package com.example.financial_engine_service.controller;

import com.example.financial_engine_service.dto.*;
import com.example.financial_engine_service.service.FinancialAnalysisQueryService;
import com.example.financial_engine_service.service.FinancialEngineOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/financial-engine")
public class FinancialEngineController {

    private final FinancialEngineOrchestrationService orchestrationService;
    private final FinancialAnalysisQueryService queryService;

    public FinancialEngineController(
            FinancialEngineOrchestrationService orchestrationService,
            FinancialAnalysisQueryService queryService) {
        this.orchestrationService = orchestrationService;
        this.queryService = queryService;
    }

    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialEngineAnalysisResponse analyze(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Valid @RequestBody FinancialSnapshotRequest request) {
        return orchestrationService.analyze(userId, request);
    }

    @GetMapping("/analyses")
    public List<FinancialAnalysisResponse> getMyAnalyses(
            @RequestHeader("X-Authenticated-User-Id") Long userId) {
        return queryService.getMyAnalyses(userId);
    }

    @GetMapping("/analyses/{analysisId}")
    public FinancialAnalysisResponse getMyAnalysis(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID analysisId) {
        return queryService.getMyAnalysis(userId, analysisId);
    }
}
