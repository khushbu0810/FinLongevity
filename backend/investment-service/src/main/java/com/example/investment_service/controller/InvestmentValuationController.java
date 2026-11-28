package com.example.investment_service.controller;

import com.example.investment_service.dto.valuation.InvestmentValuationOverviewResponse;
import com.example.investment_service.dto.valuation.InvestmentValuationRequest;
import com.example.investment_service.dto.valuation.InvestmentValuationResponse;
import com.example.investment_service.service.InvestmentValuationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments")
public class InvestmentValuationController {

    private final InvestmentValuationService valuationService;

    public InvestmentValuationController(InvestmentValuationService valuationService) {
        this.valuationService = valuationService;
    }

    @PostMapping("/{investmentId}/valuations")
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentValuationResponse recordValuation(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID investmentId,
            @Valid @RequestBody InvestmentValuationRequest request) {
        return valuationService.recordValuation(userId, investmentId, request);
    }

    @GetMapping("/{investmentId}/valuations")
    public List<InvestmentValuationResponse> getValuationHistory(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID investmentId) {
        return valuationService.getValuationHistory(userId, investmentId);
    }

    @GetMapping("/valuation-overview")
    public InvestmentValuationOverviewResponse getValuationOverview(
            @RequestHeader("X-Authenticated-User-Id") Long userId) {
        return valuationService.getValuationOverview(userId);
    }
}
