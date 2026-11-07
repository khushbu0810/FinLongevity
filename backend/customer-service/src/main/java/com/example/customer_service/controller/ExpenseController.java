package com.example.customer_service.controller;

import com.example.customer_service.dto.expenses.ExpenseRequest;
import com.example.customer_service.dto.expenses.ExpenseResponse;
import com.example.customer_service.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/me/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getMyExpenses() {
        return ResponseEntity.ok(expenseService.getMyExpenses());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ExpenseResponse>> getMyActiveExpenses() {
        return ResponseEntity.ok(expenseService.getMyActiveExpenses());
    }

    @GetMapping("/active/currency/{currency}")
    public ResponseEntity<List<ExpenseResponse>> getMyActiveExpensesByCurrency(
            @PathVariable String currency) {
        return ResponseEntity.ok(expenseService.getMyActiveExpensesByCurrency(currency));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
