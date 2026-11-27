package com.example.financial_engine_service.controller;

import com.example.financial_engine_service.dto.FinancialDashboardResponse;
import com.example.financial_engine_service.service.FinancialDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financial-engine")
public class FinancialDashboardController {

    private final FinancialDashboardService financialDashboardService;

    public FinancialDashboardController(
            FinancialDashboardService financialDashboardService) {
        this.financialDashboardService = financialDashboardService;
    }

    @GetMapping("/dashboard")
    public FinancialDashboardResponse getMyDashboard(
            @RequestHeader("X-Authenticated-User-Id") Long userId) {
        return financialDashboardService.getMyDashboard(userId);
    }
}
