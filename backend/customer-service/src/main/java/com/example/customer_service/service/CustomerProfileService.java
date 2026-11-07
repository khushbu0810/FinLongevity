package com.example.customer_service.service;

import com.example.customer_service.config.GatewayUserPrincipal;
import com.example.customer_service.repository.CustomerProfileRepository;
import com.example.customer_service.dto.profile.CustomerProfileRequest;
import com.example.customer_service.dto.profile.CustomerProfileResponse;
import com.example.customer_service.model.CustomerProfile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerProfileService {
    private final CustomerProfileRepository repository;

    public CustomerProfileService(CustomerProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerProfileResponse createMyProfile(CustomerProfileRequest request) {
        GatewayUserPrincipal user = currentUser();
        if (repository.existsByIdentityUserId(user.userId()))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A customer profile already exists for this account"
            );
        CustomerProfile profile = new CustomerProfile(
                user.userId(),
                request.firstName().trim(),
                request.lastName().trim(),
                user.email(),
                request.phone(),
                request.dateOfBirth(),
                request.gender());
        return toResponse(repository.save(profile));
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getMyProfile() {
        return toResponse(
                findProfile(currentUser().userId())
        );
    }

    @Transactional
    public CustomerProfileResponse updateMyProfile(CustomerProfileRequest request) {
        GatewayUserPrincipal user = currentUser();
        CustomerProfile profile = findProfile(user.userId());
        profile.update(
                request.firstName().trim(),
                request.lastName().trim(),
                user.email(),
                request.phone(),
                request.dateOfBirth(),
                request.gender());
        return toResponse(repository.save(profile));
    }

    private CustomerProfile findProfile(String id) {
        return repository.findByIdentityUserId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer profile not found")
                );
    }

    private GatewayUserPrincipal currentUser() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof GatewayUserPrincipal u) return u;
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authenticated user context is unavailable"
        );
    }

    private CustomerProfileResponse toResponse(CustomerProfile p) {
        return new CustomerProfileResponse(
                p.getId(),
                p.getIdentityUserId(),
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                p.getPhone(),
                p.getDateOfBirth(),
                p.getGender(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}
