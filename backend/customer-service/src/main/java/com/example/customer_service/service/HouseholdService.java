package com.example.customer_service.service;

import com.example.customer_service.config.GatewayUserPrincipal;
import com.example.customer_service.dto.household.HouseholdMemberRequest;
import com.example.customer_service.dto.household.HouseholdMemberResponse;
import com.example.customer_service.dto.household.HouseholdRequest;
import com.example.customer_service.dto.household.HouseholdResponse;
import com.example.customer_service.model.household.Household;
import com.example.customer_service.model.household.HouseholdMember;
import com.example.customer_service.model.household.HouseholdRelationship;
import com.example.customer_service.model.household.HouseholdRole;
import com.example.customer_service.repository.HouseholdMemberRepository;
import com.example.customer_service.repository.HouseholdRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    public HouseholdService(
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
    }

    @Transactional
    public HouseholdResponse createHousehold(HouseholdRequest request) {
        String userId = getAuthenticatedUserId();

        if (householdRepository.existsByOwnerIdentityUserId(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A household already exists for this customer"
            );
        }

        Household household = new Household();
        household.setOwnerIdentityUserId(userId);
        applyHouseholdRequest(household, request);

        return toHouseholdResponse(householdRepository.save(household));
    }

    @Transactional(readOnly = true)
    public HouseholdResponse getMyHousehold() {
        Household household = getOwnedHousehold(getAuthenticatedUserId());
        return toHouseholdResponse(household);
    }

    @Transactional
    public HouseholdResponse updateHousehold(HouseholdRequest request) {
        Household household = getOwnedHousehold(getAuthenticatedUserId());
        applyHouseholdRequest(household, request);
        return toHouseholdResponse(householdRepository.save(household));
    }

    @Transactional
    public HouseholdMemberResponse addMember(HouseholdMemberRequest request) {
        Household household = getOwnedHousehold(getAuthenticatedUserId());
        validateMemberRequest(request);

        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        applyMemberRequest(member, request);

        return toMemberResponse(householdMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<HouseholdMemberResponse> getMembers() {
        Household household = getOwnedHousehold(getAuthenticatedUserId());

        return householdMemberRepository
                .findAllByHousehold_IdOrderByCreatedAtDesc(household.getId())
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public HouseholdMemberResponse updateMember(UUID memberId, HouseholdMemberRequest request) {
        Household household = getOwnedHousehold(getAuthenticatedUserId());
        validateMemberRequest(request);

        HouseholdMember member = householdMemberRepository
                .findByIdAndHousehold_Id(memberId, household.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Household member not found"
                ));

        applyMemberRequest(member, request);
        return toMemberResponse(householdMemberRepository.save(member));
    }

    @Transactional
    public void deleteMember(UUID memberId) {
        Household household = getOwnedHousehold(getAuthenticatedUserId());

        HouseholdMember member = householdMemberRepository
                .findByIdAndHousehold_Id(memberId, household.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Household member not found"
                ));

        householdMemberRepository.delete(member);
    }

    private void applyHouseholdRequest(Household household, HouseholdRequest request) {
        household.setName(request.name().trim());
        household.setDescription(request.description() == null ? null : request.description().trim());
        household.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
    }

    private void validateMemberRequest(HouseholdMemberRequest request) {
        if (request.relationship() == HouseholdRelationship.SELF) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The household owner is represented by ownerIdentityUserId; SELF cannot be added as a member"
            );
        }

        if (request.role() == HouseholdRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OWNER is assigned to the household creator and cannot be assigned to a member"
            );
        }

        // Account linking needs a verified invitation/acceptance workflow.
        // Never trust an arbitrary identityUserId supplied by a household owner.
        if (request.identityUserId() != null && !request.identityUserId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Linking an existing account is not available yet; add this person as a dependant without identityUserId"
            );
        }
    }

    private void applyMemberRequest(HouseholdMember member, HouseholdMemberRequest request) {
        member.setFullName(request.fullName().trim());
        member.setRelationship(request.relationship());
        member.setRole(request.role());
        member.setDateOfBirth(request.dateOfBirth());
        member.setEmail(request.email() == null ? null : request.email().trim());
        member.setPhone(request.phone() == null ? null : request.phone().trim());
        member.setIdentityUserId(null);
    }

    private Household getOwnedHousehold(String userId) {
        return householdRepository.findByOwnerIdentityUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Household not found; create your household first"
                ));
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

    private HouseholdResponse toHouseholdResponse(Household household) {
        return new HouseholdResponse(
                household.getId(),
                household.getOwnerIdentityUserId(),
                household.getName(),
                household.getDescription(),
                household.getCurrency(),
                household.getCreatedAt(),
                household.getUpdatedAt()
        );
    }

    private HouseholdMemberResponse toMemberResponse(HouseholdMember member) {
        return new HouseholdMemberResponse(
                member.getId(),
                member.getHousehold().getId(),
                member.getIdentityUserId(),
                member.getFullName(),
                member.getRelationship(),
                member.getRole(),
                member.getDateOfBirth(),
                member.getEmail(),
                member.getPhone(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
