package com.example.customer_service.service;

import com.example.customer_service.config.GatewayUserPrincipal;
import com.example.customer_service.dto.expenses.ExpenseRequest;
import com.example.customer_service.dto.expenses.ExpenseResponse;
import com.example.customer_service.model.expenses.CustomerExpense;
import com.example.customer_service.repository.CustomerExpenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ExpenseService {

    private final CustomerExpenseRepository expenseRepository;

    public ExpenseService(CustomerExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        String userId = getAuthenticatedUserId();
        validateDates(request);

        CustomerExpense expense = new CustomerExpense();
        expense.setIdentityUserId(userId);
        applyRequest(expense, request);

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getMyExpenses() {
        String userId = getAuthenticatedUserId();

        return expenseRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getMyActiveExpenses() {
        String userId = getAuthenticatedUserId();

        return expenseRepository
                .findAllByIdentityUserIdAndActiveOrderByCreatedAtDesc(userId, true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getMyActiveExpensesByCurrency(String currency) {
        String userId = getAuthenticatedUserId();
        String normalizedCurrency = normalizeCurrency(currency);

        return expenseRepository
                .findAllByIdentityUserIdAndCurrencyAndActive(userId, normalizedCurrency, true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID expenseId, ExpenseRequest request) {
        String userId = getAuthenticatedUserId();
        validateDates(request);

        CustomerExpense expense = expenseRepository.findByIdAndIdentityUserId(expenseId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"
                ));

        applyRequest(expense, request);
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(UUID expenseId) {
        String userId = getAuthenticatedUserId();

        CustomerExpense expense = expenseRepository.findByIdAndIdentityUserId(expenseId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"
                ));

        expenseRepository.delete(expense);
    }

    private void validateDates(ExpenseRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "End date cannot be earlier than start date"
            );
        }

        if (request.startDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Start date cannot be in the future"
            );
        }
    }

    private void applyRequest(CustomerExpense expense, ExpenseRequest request) {
        expense.setName(request.name().trim());
        expense.setCategory(request.category());
        expense.setAmount(request.amount());
        expense.setCurrency(normalizeCurrency(request.currency()));
        expense.setFrequency(request.frequency());
        expense.setStartDate(request.startDate());
        expense.setEndDate(request.endDate());
        expense.setEssential(request.essential());
        expense.setActive(request.active());
        expense.setNotes(request.notes() == null ? null : request.notes().trim());
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || !currency.matches("^[A-Za-z]{3}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Currency must be a 3-letter alphabetic code"
            );
        }
        return currency.toUpperCase(Locale.ROOT);
    }

    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof GatewayUserPrincipal principal)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authenticated Gateway user is required"
            );
        }

        return principal.userId();
    }

    private ExpenseResponse toResponse(CustomerExpense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getName(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getCurrency(),
                expense.getFrequency(),
                expense.getStartDate(),
                expense.getEndDate(),
                expense.isEssential(),
                expense.isActive(),
                expense.getNotes(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
