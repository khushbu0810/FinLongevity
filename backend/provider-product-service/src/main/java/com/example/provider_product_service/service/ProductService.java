package com.example.provider_product_service.service;

import com.example.provider_product_service.dto.product.ProductRequest;
import com.example.provider_product_service.dto.product.ProductResponse;
import com.example.provider_product_service.model.product.Product;
import com.example.provider_product_service.model.product.ProductStatus;
import com.example.provider_product_service.model.product.ProductType;
import com.example.provider_product_service.model.provider.Provider;
import com.example.provider_product_service.model.provider.ProviderStatus;
import com.example.provider_product_service.repository.ProductRepository;
import com.example.provider_product_service.repository.ProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;

    public ProductService(ProductRepository productRepository, ProviderRepository providerRepository) {
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
    }

    public ProductResponse createProduct(UUID providerId, ProductRequest request) {
        Provider provider = findProvider(providerId);
        if (provider.getStatus() != ProviderStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Products can only be added to an ACTIVE provider");
        }

        validateInvestmentRange(request.minimumInvestment(), request.maximumInvestment());

        String externalCode = request.externalProductCode().trim();
        if (productRepository.existsByProviderIdAndExternalProductCode(providerId, externalCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product code already exists for this provider");
        }

        Product product = new Product();
        product.setProvider(provider);
        product.setExternalProductCode(externalCode);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setProductType(request.productType());
        product.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        product.setMinimumInvestment(request.minimumInvestment());
        product.setMaximumInvestment(request.maximumInvestment());
        product.setFeatured(request.featured());
        product.setStatus(ProductStatus.DRAFT);

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(UUID productId, ProductRequest request) {
        Product product = findProduct(productId);
        validateInvestmentRange(request.minimumInvestment(), request.maximumInvestment());

        String newCode = request.externalProductCode().trim();
        if (!product.getExternalProductCode().equals(newCode)
                && productRepository.existsByProviderIdAndExternalProductCode(
                product.getProvider().getId(), newCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product code already exists for this provider");
        }

        product.setExternalProductCode(newCode);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setProductType(request.productType());
        product.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        product.setMinimumInvestment(request.minimumInvestment());
        product.setMaximumInvestment(request.maximumInvestment());
        product.setFeatured(request.featured());

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProductStatus(UUID productId, ProductStatus status) {
        Product product = findProduct(productId);
        if (status == ProductStatus.ACTIVE
                && product.getProvider().getStatus() != ProviderStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot activate a product while its provider is not ACTIVE");
        }
        product.setStatus(status);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId) {
        return toResponse(findProduct(productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProviderProducts(UUID providerId) {
        findProvider(providerId);
        return productRepository.findAllByProviderIdOrderByCreatedAtDesc(providerId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getCatalogue(ProductType type) {
        List<Product> products = type == null
                ? productRepository.findAllByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE)
                : productRepository.findAllByProductTypeAndStatusOrderByCreatedAtDesc(
                type, ProductStatus.ACTIVE);
        return products.stream()
                .filter(p -> p.getProvider().getStatus() == ProviderStatus.ACTIVE)
                .map(this::toResponse)
                .toList();
    }

    private void validateInvestmentRange(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && max.compareTo(min) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Maximum investment cannot be less than minimum investment");
        }
    }

    private Provider findProvider(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
    }

    private Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getProvider().getId(), p.getProvider().getName(),
                p.getExternalProductCode(), p.getName(), p.getDescription(),
                p.getProductType(), p.getStatus(), p.getCurrency(),
                p.getMinimumInvestment(), p.getMaximumInvestment(), p.isFeatured(),
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
