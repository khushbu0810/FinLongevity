package com.example.financial_engine_service.controller;

import com.example.financial_engine_service.service.CustomerFinancialAnalysisService;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-engine")
@Validated
public class CustomerFinancialAnalysisController {

    private final CustomerFinancialAnalysisService customerFinancialAnalysisService;

    public CustomerFinancialAnalysisController(
            CustomerFinancialAnalysisService customerFinancialAnalysisService) {
        this.customerFinancialAnalysisService = customerFinancialAnalysisService;
    }

    @PostMapping("/analyze/me")
    public CustomerFinancialAnalysisService.CustomerAnalysisResult analyzeMyFinancials(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @RequestHeader("X-Authenticated-User-Email") String email,
            @RequestHeader("X-Authenticated-User-Role") String role,
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$",
                    message = "Currency must be a 3-letter ISO code")
            String currency) {

        return customerFinancialAnalysisService.analyzeMyFinancials(
                userId, email, role, currency);
    }
}
