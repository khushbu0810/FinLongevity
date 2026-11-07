package com.example.customer_service.controller;

import com.example.customer_service.dto.risk_profile.RiskProfileRequest;
import com.example.customer_service.dto.risk_profile.RiskProfileResponse;
import com.example.customer_service.service.RiskProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/me/risk-profile")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    public RiskProfileController(RiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    @PutMapping
    public ResponseEntity<RiskProfileResponse> saveOrUpdate(
            @Valid @RequestBody RiskProfileRequest request) {
        RiskProfileResponse response = riskProfileService.saveOrUpdate(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<RiskProfileResponse> getMyRiskProfile() {
        return ResponseEntity.ok(riskProfileService.getMyRiskProfile());
    }
}
