package com.example.provider_product_service.controller;

import com.example.provider_product_service.dto.product.ProductRequest;
import com.example.provider_product_service.dto.product.ProductResponse;
import com.example.provider_product_service.model.product.ProductStatus;
import com.example.provider_product_service.model.product.ProductType;
import com.example.provider_product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/{providerId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Parameter(
                    name = "providerId",
                    description = "Enter the Provider UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID providerId,
            @Valid @RequestBody ProductRequest request) {
        return productService.createProduct(providerId, request);
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(
            @Parameter(
                    name = "productId",
                    description = "Enter the Product UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID productId,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(productId, request);
    }

    @PatchMapping("/{productId}/status")
    public ProductResponse updateProductStatus(
            @Parameter(
                    name = "productId",
                    description = "Enter the Product UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID productId,
            @RequestParam(name = "status") ProductStatus status) {
        return productService.updateProductStatus(productId, status);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(
            @Parameter(
                    name = "productId",
                    description = "Enter the Product UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID productId) {
        return productService.getProduct(productId);
    }

    @GetMapping("/{providerId}/products")
    public List<ProductResponse> getProviderProducts(
            @Parameter(
                    name = "providerId",
                    description = "Enter the Provider UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID providerId) {
        return productService.getProviderProducts(providerId);
    }

    @GetMapping("/catalogue")
    public List<ProductResponse> getCatalogue(
            @RequestParam(name = "type", required = false) ProductType type) {
        return productService.getCatalogue(type);
    }
}
