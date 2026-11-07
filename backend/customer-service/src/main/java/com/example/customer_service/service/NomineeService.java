package com.example.customer_service.service;

import com.example.customer_service.config.GatewayUserPrincipal;
import com.example.customer_service.dto.nominee.NomineeRequest;
import com.example.customer_service.dto.nominee.NomineeResponse;
import com.example.customer_service.model.nominee.Nominee;
import com.example.customer_service.repository.NomineeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NomineeService {

    private static final BigDecimal MAX_TOTAL_ALLOCATION = new BigDecimal("100.00");

    private final NomineeRepository nomineeRepository;

    public NomineeService(NomineeRepository nomineeRepository) {
        this.nomineeRepository = nomineeRepository;
    }

    public NomineeResponse createNominee(NomineeRequest request) {
        String userId = currentUserId();
        validateNomineeDetails(request);
        validateAllocation(userId, request.allocationPercentage(), null);

        Nominee nominee = new Nominee();
        nominee.setIdentityUserId(userId);
        applyRequest(nominee, request);

        return toResponse(nomineeRepository.save(nominee));
    }

    @Transactional(readOnly = true)
    public List<NomineeResponse> getNominees() {
        return nomineeRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(currentUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NomineeResponse updateNominee(UUID nomineeId, NomineeRequest request) {
        String userId = currentUserId();

        Nominee nominee = nomineeRepository.findByIdAndIdentityUserId(nomineeId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nominee not found"));

        validateNomineeDetails(request);
        validateAllocation(userId, request.allocationPercentage(), nomineeId);

        applyRequest(nominee, request);
        return toResponse(nomineeRepository.save(nominee));
    }

    public void deleteNominee(UUID nomineeId) {
        Nominee nominee = nomineeRepository.findByIdAndIdentityUserId(nomineeId, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nominee not found"));

        nomineeRepository.delete(nominee);
    }

    private void validateAllocation(String userId, BigDecimal requestedAllocation, UUID excludedNomineeId) {
        if (requestedAllocation == null
                || requestedAllocation.compareTo(BigDecimal.ZERO) <= 0
                || requestedAllocation.compareTo(MAX_TOTAL_ALLOCATION) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Allocation must be greater than 0 and at most 100%");
        }

        BigDecimal existingAllocation = nomineeRepository
                .findAllByIdentityUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(existing -> excludedNomineeId == null
                        || !existing.getId().equals(excludedNomineeId))
                .map(Nominee::getAllocationPercentage)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = existingAllocation.add(requestedAllocation);
        if (newTotal.compareTo(MAX_TOTAL_ALLOCATION) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total nominee allocation cannot exceed 100%. Current allocation: "
                            + existingAllocation + "%, requested: " + requestedAllocation + "%");
        }
    }

    private void validateNomineeDetails(NomineeRequest request) {
        if (request.dateOfBirth() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date of birth is required");
        }

        if (request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date of birth cannot be in the future");
        }

        boolean minor = Period.between(request.dateOfBirth(), LocalDate.now()).getYears() < 18;
        if (minor && (request.guardianName() == null || request.guardianName().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Guardian name is required for a nominee under 18");
        }
    }

    private void applyRequest(Nominee nominee, NomineeRequest request) {
        nominee.setFullName(request.fullName().trim());
        nominee.setRelationship(request.relationship());
        nominee.setDateOfBirth(request.dateOfBirth());
        nominee.setEmail(request.email());
        nominee.setPhone(request.phone());
        nominee.setAllocationPercentage(request.allocationPercentage());
        nominee.setGuardianName(request.guardianName());
        nominee.setNotes(request.notes());
    }

    private NomineeResponse toResponse(Nominee nominee) {
        return new NomineeResponse(
                nominee.getId(),
                nominee.getFullName(),
                nominee.getRelationship(),
                nominee.getDateOfBirth(),
                nominee.getEmail(),
                nominee.getPhone(),
                nominee.getAllocationPercentage(),
                nominee.getGuardianName(),
                nominee.getNotes(),
                nominee.getCreatedAt(),
                nominee.getUpdatedAt()
        );
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof GatewayUserPrincipal principal)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authenticated user context is missing");
        }
        return principal.userId();
    }
}
