package com.example.customer_service.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Establishes the local Spring Security principal from identity headers
 * inserted by the trusted API Gateway after JWT validation.
 * <p>
 * This filter does not parse or validate JWTs. The Gateway is the only
 * component responsible for JWT signature and expiry validation.
 */
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String email = request.getHeader("X-Authenticated-User-Email");
        String userId = request.getHeader("X-Authenticated-User-Id");
        String role = request.getHeader("X-Authenticated-User-Role");

        if (blank(userId) || blank(email) || blank(role)) {
            response.sendError(401, "Missing authenticated-user context. Access this service through the API Gateway.");
            return;
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);

        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring(5);
        }
        var principal = new GatewayUserPrincipal(
                userId.trim(), email.trim(), normalizedRole
        );
        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
