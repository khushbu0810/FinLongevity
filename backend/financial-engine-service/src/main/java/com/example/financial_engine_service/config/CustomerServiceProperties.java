package com.example.financial_engine_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.customer")
public record CustomerServiceProperties(String baseUrl) {
}
