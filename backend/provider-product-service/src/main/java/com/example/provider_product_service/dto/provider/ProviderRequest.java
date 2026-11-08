package com.example.provider_product_service.dto.provider;

import com.example.provider_product_service.model.provider.ProviderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProviderRequest(
        @NotBlank
        @Size(max = 40)
        String providerCode,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        ProviderType type,

        @NotBlank
        @Size(max = 80)
        String registrationNumber,

        @NotBlank
        @Email
        @Size(max = 180)
        String contactEmail,

        @Size(max = 25)
        String contactPhone,

        @Size(max = 250)
        String website,

        @Size(max = 2000)
        String description
) {
}
