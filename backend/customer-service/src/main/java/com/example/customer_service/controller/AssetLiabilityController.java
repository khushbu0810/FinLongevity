package com.example.customer_service.controller;

import com.example.customer_service.dto.asset_liabilities.*;
import com.example.customer_service.service.AssetLiabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/me")
public class AssetLiabilityController {
    private final AssetLiabilityService service;

    public AssetLiabilityController(AssetLiabilityService service) {
        this.service = service;
    }

    @PostMapping("/assets")
    public ResponseEntity<AssetResponse> createAsset(@Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAsset(request));
    }

    @GetMapping("/assets")
    public ResponseEntity<List<AssetResponse>> getAssets() {
        return ResponseEntity.ok(service.getAssets());
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<AssetResponse> updateAsset(@PathVariable UUID id, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(service.updateAsset(id, request));
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable UUID id) {
        service.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/liabilities")
    public ResponseEntity<LiabilityResponse> createLiability(@Valid @RequestBody LiabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLiability(request));
    }

    @GetMapping("/liabilities")
    public ResponseEntity<List<LiabilityResponse>> getLiabilities() {
        return ResponseEntity.ok(service.getLiabilities());
    }

    @PutMapping("/liabilities/{id}")
    public ResponseEntity<LiabilityResponse> updateLiability(@PathVariable UUID id, @Valid @RequestBody LiabilityRequest request) {
        return ResponseEntity.ok(service.updateLiability(id, request));
    }

    @DeleteMapping("/liabilities/{id}")
    public ResponseEntity<Void> deleteLiability(@PathVariable UUID id) {
        service.deleteLiability(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/net-worth")
    public ResponseEntity<NetWorthResponse> netWorth(@RequestParam(name = "currency", defaultValue = "INR") String currency) {
        return ResponseEntity.ok(service.calculateNetWorth(currency));
    }
}
