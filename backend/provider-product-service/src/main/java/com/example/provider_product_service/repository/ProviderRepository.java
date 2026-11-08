package com.example.provider_product_service.repository;

import com.example.provider_product_service.model.provider.Provider;
import com.example.provider_product_service.model.provider.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    boolean existsByProviderCode(String providerCode);

    boolean existsByRegistrationNumber(String registrationNumber);

    Optional<Provider> findByProviderCode(String providerCode);

    List<Provider> findAllByStatusOrderByCreatedAtDesc(ProviderStatus status);
}
