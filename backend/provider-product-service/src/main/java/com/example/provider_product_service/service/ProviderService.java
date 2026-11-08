package com.example.provider_product_service.service;

import com.example.provider_product_service.dto.provider.ProviderRequest;
import com.example.provider_product_service.dto.provider.ProviderResponse;
import com.example.provider_product_service.model.provider.Provider;
import com.example.provider_product_service.model.provider.ProviderStatus;
import com.example.provider_product_service.repository.ProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public ProviderResponse createProvider(ProviderRequest request) {
        String code = request.providerCode().trim().toUpperCase();
        String registrationNumber = request.registrationNumber().trim();

        if (providerRepository.existsByProviderCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Provider code already exists");
        }
        if (providerRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registration number already exists");
        }

        Provider provider = new Provider();
        provider.setProviderCode(code);
        provider.setName(request.name().trim());
        provider.setType(request.type());
        provider.setRegistrationNumber(registrationNumber);
        provider.setContactEmail(request.contactEmail().trim().toLowerCase());
        provider.setContactPhone(request.contactPhone());
        provider.setWebsite(request.website());
        provider.setDescription(request.description());
        provider.setStatus(ProviderStatus.DRAFT);

        return toResponse(providerRepository.save(provider));
    }

    @Transactional(readOnly = true)
    public ProviderResponse getProvider(UUID providerId) {
        return toResponse(findProvider(providerId));
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> getProviders(ProviderStatus status) {
        List<Provider> providers = status == null
                ? providerRepository.findAll()
                : providerRepository.findAllByStatusOrderByCreatedAtDesc(status);
        return providers.stream().map(this::toResponse).toList();
    }

    public ProviderResponse updateProviderStatus(UUID providerId, ProviderStatus status) {
        Provider provider = findProvider(providerId);
        provider.setStatus(status);
        return toResponse(providerRepository.save(provider));
    }

    private Provider findProvider(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
    }

    private ProviderResponse toResponse(Provider p) {
        return new ProviderResponse(
                p.getId(), p.getProviderCode(), p.getName(), p.getType(),
                p.getRegistrationNumber(), p.getContactEmail(), p.getContactPhone(),
                p.getWebsite(), p.getStatus(), p.getDescription(),
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
