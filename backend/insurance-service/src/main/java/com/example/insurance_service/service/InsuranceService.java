package com.example.insurance_service.service;

import com.example.insurance_service.dto.*;
import com.example.insurance_service.model.*;
import com.example.insurance_service.repository.InsuranceApplicationRepository;
import com.example.insurance_service.repository.InsurancePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InsuranceService {

    private final InsuranceApplicationRepository applicationRepository;
    private final InsurancePolicyRepository policyRepository;

    public InsuranceService(
            InsuranceApplicationRepository applicationRepository,
            InsurancePolicyRepository policyRepository) {
        this.applicationRepository = applicationRepository;
        this.policyRepository = policyRepository;
    }

    public InsuranceApplicationResponse createApplication(
            Long userId, InsuranceApplicationRequest request) {
        InsuranceApplication application = new InsuranceApplication();
        application.setIdentityUserId(userId);
        application.setProviderProductId(request.providerProductId());
        application.setProductNameSnapshot(request.productNameSnapshot().trim());
        application.setInsuranceType(request.insuranceType());
        application.setApplicantName(request.applicantName().trim());
        application.setApplicantDateOfBirth(request.applicantDateOfBirth());
        application.setCoverageAmount(request.coverageAmount());
        application.setCurrency(request.currency().trim().toUpperCase());
        application.setStatus(InsuranceApplicationStatus.DRAFT);

        return toApplicationResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<InsuranceApplicationResponse> getMyApplications(Long userId) {
        return applicationRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toApplicationResponse).toList();
    }

    @Transactional(readOnly = true)
    public InsuranceApplicationResponse getMyApplication(Long userId, UUID applicationId) {
        return toApplicationResponse(findApplication(userId, applicationId));
    }

    public InsuranceApplicationResponse submitApplication(Long userId, UUID applicationId) {
        InsuranceApplication application = findApplication(userId, applicationId);
        if (application.getStatus() != InsuranceApplicationStatus.DRAFT) {
            throw new IllegalStateException("Only draft applications can be submitted");
        }
        application.setStatus(InsuranceApplicationStatus.SUBMITTED);
        application.setSubmittedAt(Instant.now());
        return toApplicationResponse(applicationRepository.save(application));
    }

    public InsuranceApplicationResponse decideApplication(
            Long userId, UUID applicationId, InsuranceDecisionRequest request) {
        InsuranceApplication application = findApplication(userId, applicationId);
        if (application.getStatus() != InsuranceApplicationStatus.SUBMITTED
                && application.getStatus() != InsuranceApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only submitted or under-review applications can be decided");
        }

        application.setDecisionAt(Instant.now());
        application.setDecisionNote(request.decisionNote());

        if (request.decision() == InsuranceDecisionRequest.Decision.REJECT) {
            application.setStatus(InsuranceApplicationStatus.REJECTED);
            return toApplicationResponse(applicationRepository.save(application));
        }

        if (request.premiumAmount() == null || request.premiumFrequency() == null
                || request.startDate() == null) {
            throw new IllegalArgumentException(
                    "Approval requires premiumAmount, premiumFrequency and startDate");
        }
        if (request.premiumAmount().signum() <= 0) {
            throw new IllegalArgumentException("premiumAmount must be greater than zero");
        }
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
        if (request.nextPremiumDueDate() != null
                && request.nextPremiumDueDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("nextPremiumDueDate cannot be before startDate");
        }

        application.setStatus(InsuranceApplicationStatus.APPROVED);
        applicationRepository.save(application);

        InsurancePolicy policy = new InsurancePolicy();
        policy.setIdentityUserId(userId);
        policy.setApplication(application);
        policy.setPolicyNumber("DEMO-POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        policy.setProviderProductId(application.getProviderProductId());
        policy.setCoverageAmount(application.getCoverageAmount());
        policy.setPremiumAmount(request.premiumAmount());
        policy.setCurrency(application.getCurrency());
        policy.setPremiumFrequency(request.premiumFrequency());
        policy.setStatus(InsurancePolicyStatus.ACTIVE);
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());
        policy.setNextPremiumDueDate(request.nextPremiumDueDate());

        policyRepository.save(policy);
        return toApplicationResponse(application);
    }

    @Transactional(readOnly = true)
    public List<InsurancePolicyResponse> getMyPolicies(Long userId) {
        return policyRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toPolicyResponse).toList();
    }

    @Transactional(readOnly = true)
    public InsurancePolicyResponse getMyPolicy(Long userId, UUID policyId) {
        InsurancePolicy policy = policyRepository.findByIdAndIdentityUserId(policyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Insurance policy not found"));
        return toPolicyResponse(policy);
    }

    private InsuranceApplication findApplication(Long userId, UUID applicationId) {
        return applicationRepository.findByIdAndIdentityUserId(applicationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Insurance application not found"));
    }

    private InsuranceApplicationResponse toApplicationResponse(InsuranceApplication a) {
        return new InsuranceApplicationResponse(
                a.getId(), a.getProviderProductId(), a.getProductNameSnapshot(),
                a.getInsuranceType(), a.getApplicantName(), a.getApplicantDateOfBirth(),
                a.getCoverageAmount(), a.getCurrency(), a.getStatus(), a.getSubmittedAt(),
                a.getDecisionAt(), a.getDecisionNote(), a.getCreatedAt(), a.getUpdatedAt());
    }

    private InsurancePolicyResponse toPolicyResponse(InsurancePolicy p) {
        return new InsurancePolicyResponse(
                p.getId(), p.getApplication().getId(), p.getPolicyNumber(),
                p.getProviderProductId(), p.getCoverageAmount(), p.getPremiumAmount(),
                p.getCurrency(), p.getPremiumFrequency(), p.getStatus(), p.getStartDate(),
                p.getEndDate(), p.getNextPremiumDueDate(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
