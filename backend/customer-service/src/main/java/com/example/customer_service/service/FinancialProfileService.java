package com.example.customer_service.service;

import com.example.customer_service.config.GatewayUserPrincipal;
import com.example.customer_service.dto.income.EmploymentRequest;
import com.example.customer_service.dto.income.EmploymentResponse;
import com.example.customer_service.dto.income.IncomeSourceRequest;
import com.example.customer_service.dto.income.IncomeSourceResponse;
import com.example.customer_service.model.income.EmploymentDetails;
import com.example.customer_service.model.income.IncomeSource;
import com.example.customer_service.repository.EmploymentDetailsRepository;
import com.example.customer_service.repository.IncomeSourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FinancialProfileService {
    private final EmploymentDetailsRepository employmentRepository;
    private final IncomeSourceRepository incomeRepository;

    public FinancialProfileService(EmploymentDetailsRepository employmentRepository,
                                   IncomeSourceRepository incomeRepository) {
        this.employmentRepository = employmentRepository;
        this.incomeRepository = incomeRepository;
    }

    @Transactional
    public EmploymentResponse saveEmployment(EmploymentRequest request) {
        String userId = currentUserId();
        EmploymentDetails empDetails = employmentRepository.findByIdentityUserId(userId)
                .orElseGet(() -> new EmploymentDetails(
                        userId,
                        request.employmentType(),
                        request.employerName(),
                        request.designation(),
                        request.employmentStartDate(),
                        request.employmentEndDate(),
                        request.currentlyEmployed())
                );
        empDetails.update(
                request.employmentType(),
                request.employerName(),
                request.designation(),
                request.employmentStartDate(),
                request.employmentEndDate(),
                request.currentlyEmployed()
        );
        return employmentResponse(employmentRepository.save(empDetails));
    }

    @Transactional(readOnly = true)
    public EmploymentResponse getEmployment() {
        EmploymentDetails e = employmentRepository.findByIdentityUserId(currentUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employment details not found")
                );
        return employmentResponse(e);
    }

    @Transactional
    public IncomeSourceResponse addIncome(IncomeSourceRequest request) {
        IncomeSource income = new IncomeSource(
                currentUserId(),
                request.incomeType(),
                request.sourceName().trim(),
                request.amount(),
                request.frequency(),
                request.currency(),
                request.active()
        );
        return incomeResponse(incomeRepository.save(income));
    }

    @Transactional(readOnly = true)
    public List<IncomeSourceResponse> getIncomeSources() {
        return incomeRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(currentUserId())
                .stream().map(this::incomeResponse).toList();
    }

    @Transactional
    public IncomeSourceResponse updateIncome(UUID id, IncomeSourceRequest request) {
        IncomeSource income = ownedIncome(id);
        income.update(
                request.incomeType(),
                request.sourceName().trim(),
                request.amount(),
                request.frequency(),
                request.currency(),
                request.active()
        );
        return incomeResponse(incomeRepository.save(income));
    }

    @Transactional
    public void deleteIncome(UUID id) {
        incomeRepository.delete(ownedIncome(id));
    }

    private IncomeSource ownedIncome(UUID id) {
        return incomeRepository.findByIdAndIdentityUserId(id, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Income source not found")
                );
    }

    private String currentUserId() {
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (principal instanceof GatewayUserPrincipal user) {
            return user.userId();
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authenticated user context unavailable"
        );
    }

    private EmploymentResponse employmentResponse(EmploymentDetails response) {
        return new EmploymentResponse(
                response.getId(),
                response.getEmploymentType(),
                response.getEmployerName(),
                response.getDesignation(),
                response.getEmploymentStartDate(),
                response.getEmploymentEndDate(),
                response.isCurrentlyEmployed(),
                response.getCreatedAt(),
                response.getUpdatedAt()
        );
    }

    private IncomeSourceResponse incomeResponse(IncomeSource income) {
        return new IncomeSourceResponse(income.getId(),
                income.getIncomeType(),
                income.getSourceName(),
                income.getAmount(),
                income.getFrequency(),
                income.getCurrency(),
                income.isActive(),
                income.getCreatedAt(),
                income.getUpdatedAt()
        );
    }
}
