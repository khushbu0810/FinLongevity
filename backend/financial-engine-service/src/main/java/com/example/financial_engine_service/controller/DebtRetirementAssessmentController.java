package com.example.financial_engine_service.controller;

import com.example.financial_engine_service.dto.debt.DebtRetirementAssessmentResponse;
import com.example.financial_engine_service.service.CustomerRetirementPlanningPrefillService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-engine/debt")
@Validated
public class DebtRetirementAssessmentController {

    private final CustomerRetirementPlanningPrefillService prefillService;

    public DebtRetirementAssessmentController(
            CustomerRetirementPlanningPrefillService prefillService) {
        this.prefillService = prefillService;
    }

    @GetMapping("/retirement-assessment/me")
    public DebtRetirementAssessmentResponse getMyDebtRetirementAssessment(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @RequestHeader("X-Authenticated-User-Email") String email,
            @RequestHeader("X-Authenticated-User-Role") String role,
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
            @RequestParam(required = false)
            @Min(1) @Max(120) Integer plannedRetirementAge) {

        return prefillService.getMyDebtRetirementAssessment(
                userId, email, role, currency, plannedRetirementAge
        );
    }
}