package com.example.customer_service.controller;


import com.example.customer_service.dto.income.EmploymentRequest;
import com.example.customer_service.dto.income.EmploymentResponse;
import com.example.customer_service.dto.income.IncomeSourceRequest;
import com.example.customer_service.dto.income.IncomeSourceResponse;
import com.example.customer_service.service.FinancialProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/me")
@PreAuthorize("hasRole('CUSTOMER')")
public class FinancialProfileController {
    private final FinancialProfileService service;

    public FinancialProfileController(FinancialProfileService service) {
        this.service = service;
    }

    @PutMapping("/employment")
    public EmploymentResponse saveEmployment(@Valid @RequestBody EmploymentRequest request) {
        return service.saveEmployment(request);
    }

    @GetMapping("/employment")
    public EmploymentResponse getEmployment() {
        return service.getEmployment();
    }

    @PostMapping("/income-sources")
    @ResponseStatus(HttpStatus.CREATED)
    public IncomeSourceResponse addIncome(@Valid @RequestBody IncomeSourceRequest request) {
        return service.addIncome(request);
    }

    @GetMapping("/income-sources")
    public List<IncomeSourceResponse> getIncomeSources() {
        return service.getIncomeSources();
    }

    @PutMapping("/income-sources/{id}")
    public IncomeSourceResponse updateIncome(@PathVariable UUID id,
                                             @Valid @RequestBody IncomeSourceRequest request) {
        return service.updateIncome(id, request);
    }

    @DeleteMapping("/income-sources/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncome(@PathVariable UUID id) {
        service.deleteIncome(id);
    }
}
