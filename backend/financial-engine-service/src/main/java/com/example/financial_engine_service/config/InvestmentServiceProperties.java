package com.example.financial_engine_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.investment")
public record InvestmentServiceProperties(String baseUrl) {
}
