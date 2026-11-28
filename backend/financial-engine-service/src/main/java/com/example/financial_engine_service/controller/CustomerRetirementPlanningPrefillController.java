package com.example.financial_engine_service.controller;

import com.example.financial_engine_service.dto.retirement.RetirementPlanningPrefillResponse;
import com.example.financial_engine_service.service.CustomerRetirementPlanningPrefillService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-engine/retirement")
@Validated
public class CustomerRetirementPlanningPrefillController {

    private final CustomerRetirementPlanningPrefillService prefillService;

    public CustomerRetirementPlanningPrefillController(
            CustomerRetirementPlanningPrefillService prefillService) {
        this.prefillService = prefillService;
    }

    /** Returns editable suggestions from existing records; it does not persist a projection. */
    @GetMapping("/prefill/me")
    public RetirementPlanningPrefillResponse getMyRetirementPlanningPrefill(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @RequestHeader("X-Authenticated-User-Email") String email,
            @RequestHeader("X-Authenticated-User-Role") String role,
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter ISO code")
            String currency,
            @RequestParam(defaultValue = "90")
            @Min(value = 0, message = "Maximum valuation age cannot be negative")
            @Max(value = 3650, message = "Maximum valuation age cannot exceed 3650 days")
            long maximumValuationAgeDays) {
        return prefillService.getMyRetirementPlanningPrefill(
                userId, email, role, currency, maximumValuationAgeDays);
    }
}
