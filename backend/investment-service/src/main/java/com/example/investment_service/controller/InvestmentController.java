package com.example.investment_service.controller;

import com.example.investment_service.dto.investment.InvestmentRequest;
import com.example.investment_service.dto.investment.InvestmentResponse;
import com.example.investment_service.dto.transaction.InvestmentTransactionRequest;
import com.example.investment_service.dto.transaction.InvestmentTransactionResponse;
import com.example.investment_service.service.InvestmentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    // This header must be injected by the trusted API Gateway after JWT validation.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponse createInvestment(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Valid @RequestBody InvestmentRequest request) {
        return investmentService.createInvestment(userId, request);
    }

    @GetMapping
    public List<InvestmentResponse> getMyInvestments(
            @RequestHeader("X-Authenticated-User-Id") Long userId) {
        return investmentService.getMyInvestments(userId);
    }

    @GetMapping("/{investmentId}")
    public InvestmentResponse getMyInvestment(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Parameter(
                    name = "investmentId",
                    description = "Enter the InvestmentId UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID investmentId) {
        return investmentService.getMyInvestment(userId, investmentId);
    }

    @PatchMapping("/{investmentId}/cancel")
    public InvestmentResponse cancelDraft(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Parameter(
                    name = "investmentId",
                    description = "Enter the InvestmentId UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID investmentId) {
        return investmentService.cancelDraft(userId, investmentId);
    }

    @PostMapping("/{investmentId}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentTransactionResponse addTransaction(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Parameter(
                    name = "investmentId",
                    description = "Enter the InvestmentId UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID investmentId,
            @Valid @RequestBody InvestmentTransactionRequest request) {
        return investmentService.addTransaction(userId, investmentId, request);
    }

    @GetMapping("/{investmentId}/transactions")
    public List<InvestmentTransactionResponse> getMyTransactions(
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @Parameter(
                    name = "investmentId",
                    description = "Enter the InvestmentId UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID investmentId) {
        return investmentService.getMyTransactions(userId, investmentId);
    }
}
