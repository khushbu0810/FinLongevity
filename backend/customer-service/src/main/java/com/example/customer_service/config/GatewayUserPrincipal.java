package com.example.customer_service.config;

public record GatewayUserPrincipal(
        String userId, String email, String role
) { }
