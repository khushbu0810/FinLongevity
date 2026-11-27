package com.example.insurance_service.controller;

import com.example.insurance_service.dto.*;
import com.example.insurance_service.service.InsuranceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

    private final InsuranceService insuranceService;

    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public InsuranceApplicationResponse createApplication(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Valid @RequestBody InsuranceApplicationRequest request) {
        return insuranceService.createApplication(userId, request);
    }

    @GetMapping("/applications")
    public List<InsuranceApplicationResponse> getMyApplications(
            @RequestHeader("X-Authenticated-User-Id") Long userId) {
        return insuranceService.getMyApplications(userId);
    }

    @GetMapping("/applications/{applicationId}")
    public InsuranceApplicationResponse getMyApplication(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID applicationId) {
        return insuranceService.getMyApplication(userId, applicationId);
    }

    @PatchMapping("/applications/{applicationId}/submit")
    public InsuranceApplicationResponse submitApplication(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID applicationId) {
        return insuranceService.submitApplication(userId, applicationId);
    }

    // Demo-only decision endpoint. Production decisions must be restricted to authorized insurer/admin roles.
    @PatchMapping("/applications/{applicationId}/decision")
    public InsuranceApplicationResponse decideApplication(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID applicationId,
            @Valid @RequestBody InsuranceDecisionRequest request) {
        return insuranceService.decideApplication(userId, applicationId, request);
    }

    @GetMapping("/policies")
    public List<InsurancePolicyResponse> getMyPolicies(
            @RequestHeader("X-Authenticated-User-Id") Long userId) {
        return insuranceService.getMyPolicies(userId);
    }

    @GetMapping("/policies/{policyId}")
    public InsurancePolicyResponse getMyPolicy(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @PathVariable UUID policyId) {
        return insuranceService.getMyPolicy(userId, policyId);
    }
}
