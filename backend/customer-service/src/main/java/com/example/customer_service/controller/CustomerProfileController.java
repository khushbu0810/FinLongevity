package com.example.customer_service.controller;

import com.example.customer_service.dto.CustomerProfileRequest;
import com.example.customer_service.dto.CustomerProfileResponse;
import com.example.customer_service.service.CustomerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/me")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProfileController {
    private final CustomerProfileService service;

    public CustomerProfileController(CustomerProfileService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerProfileResponse create(@Valid @RequestBody CustomerProfileRequest request) {
        return service.createMyProfile(request);
    }

    @GetMapping
    public CustomerProfileResponse get() {
        return service.getMyProfile();
    }

    @PutMapping
    public CustomerProfileResponse update(@Valid @RequestBody CustomerProfileRequest request) {
        return service.updateMyProfile(request);
    }
}
