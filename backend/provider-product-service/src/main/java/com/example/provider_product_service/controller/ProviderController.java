package com.example.provider_product_service.controller;

import com.example.provider_product_service.dto.provider.ProviderRequest;
import com.example.provider_product_service.dto.provider.ProviderResponse;
import com.example.provider_product_service.model.provider.ProviderStatus;
import com.example.provider_product_service.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderResponse createProvider(@Valid @RequestBody ProviderRequest request) {
        return providerService.createProvider(request);
    }

    @GetMapping("/{providerId}")
    public ProviderResponse getProvider(@PathVariable UUID providerId) {
        return providerService.getProvider(providerId);
    }

    @GetMapping
    public List<ProviderResponse> getProviders(
            @RequestParam(name = "status", required = false) ProviderStatus status) {
        return providerService.getProviders(status);
    }

    @PatchMapping("/{providerId}/status")
    public ProviderResponse updateProviderStatus(
            @PathVariable UUID providerId,
            @RequestParam(name = "status") ProviderStatus status) {
        return providerService.updateProviderStatus(providerId, status);
    }
}
