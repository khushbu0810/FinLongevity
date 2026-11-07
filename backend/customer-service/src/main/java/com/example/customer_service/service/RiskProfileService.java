package com.example.customer_service.service;

import com.example.customer_service.config.GatewayUserPrincipal;
import com.example.customer_service.dto.risk_profile.RiskProfileRequest;
import com.example.customer_service.dto.risk_profile.RiskProfileResponse;
import com.example.customer_service.model.risk_profile.CustomerRiskProfile;
import com.example.customer_service.repository.RiskProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;

@Service
public class RiskProfileService {

    private final RiskProfileRepository riskProfileRepository;

    public RiskProfileService(RiskProfileRepository riskProfileRepository) {
        this.riskProfileRepository = riskProfileRepository;
    }

    @Transactional
    public RiskProfileResponse saveOrUpdate(RiskProfileRequest request) {
        String identityUserId = getAuthenticatedUserId();

        CustomerRiskProfile profile = riskProfileRepository
                .findByIdentityUserId(identityUserId)
                .orElseGet(CustomerRiskProfile::new);

        // The user ID always comes from the trusted Gateway principal,
        // never from client-provided request data.
        profile.setIdentityUserId(identityUserId);
        profile.setRiskTolerance(request.riskTolerance());
        profile.setInvestmentExperience(request.investmentExperience());
        profile.setInvestmentHorizon(request.investmentHorizon());
        profile.setLiquidityRequirement(request.liquidityRequirement());
        profile.setInvestmentPreferences(new HashSet<>(request.investmentPreferences()));
        profile.setFinancialObjectives(new HashSet<>(request.financialObjectives()));

        CustomerRiskProfile saved = riskProfileRepository.save(profile);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RiskProfileResponse getMyRiskProfile() {
        String identityUserId = getAuthenticatedUserId();

        CustomerRiskProfile profile = riskProfileRepository
                .findByIdentityUserId(identityUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Risk profile not found for this customer"
                ));

        return toResponse(profile);
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

    private RiskProfileResponse toResponse(CustomerRiskProfile profile) {
        return new RiskProfileResponse(
                profile.getId(),
                profile.getRiskTolerance(),
                profile.getInvestmentExperience(),
                profile.getInvestmentHorizon(),
                profile.getLiquidityRequirement(),
                new HashSet<>(profile.getInvestmentPreferences()),
                new HashSet<>(profile.getFinancialObjectives()),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
