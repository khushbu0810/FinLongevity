package com.example.customer_service.controller;

import com.example.customer_service.dto.expenses.ExpenseRequest;
import com.example.customer_service.dto.expenses.ExpenseResponse;
import com.example.customer_service.service.ExpenseService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
            @Parameter(
                    name = "currency",
                    description = "Enter the currency",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "INR"
            )
            @PathVariable String currency) {
        return ResponseEntity.ok(expenseService.getMyActiveExpensesByCurrency(currency));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @Parameter(
                    name = "expenseId",
                    description = "Enter the expense UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(
                    name = "expenseId",
                    description = "Enter the expense UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
