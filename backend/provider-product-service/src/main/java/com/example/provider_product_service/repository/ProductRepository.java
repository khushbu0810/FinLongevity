package com.example.provider_product_service.repository;

import com.example.provider_product_service.model.product.Product;
import com.example.provider_product_service.model.product.ProductStatus;
import com.example.provider_product_service.model.product.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByProviderIdAndExternalProductCode(UUID providerId, String externalProductCode);

    Optional<Product> findByIdAndProviderId(UUID id, UUID providerId);

    List<Product> findAllByProviderIdOrderByCreatedAtDesc(UUID providerId);

    List<Product> findAllByStatusOrderByCreatedAtDesc(ProductStatus status);

    List<Product> findAllByProductTypeAndStatusOrderByCreatedAtDesc(
            ProductType productType, ProductStatus status);
}
