package com.example.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private static final String REGISTER_PATH = "/api/identity/auth/register";
    private static final String LOGIN_PATH = "/api/identity/auth/login";
    private static final String IDENTITY_HEALTH_PATH = "/api/identity/health";

    private final SecretKey signingKey;

    public JwtGatewayFilter(
            @Value("${security.jwt.secret-base64}") String secretBase64
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path) || !path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            Object userIdClaim = claims.get("userId");
            Object roleClaim = claims.get("role");

            if (email == null || email.isBlank()
                    || userIdClaim == null || roleClaim == null) {
                return unauthorized(exchange);
            }

            String userId = userIdClaim.toString();
            String role = roleClaim.toString();

            // Never trust identity headers supplied by the caller.
            ServerHttpRequest authenticatedRequest = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove("X-Authenticated-User-Id");
                        headers.remove("X-Authenticated-User-Email");
                        headers.remove("X-Authenticated-User-Role");
                        headers.set("X-Authenticated-User-Id", userId);
                        headers.set("X-Authenticated-User-Email", email);
                        headers.set("X-Authenticated-User-Role", role);
                    })
                    .build();

            return chain.filter(exchange.mutate()
                    .request(authenticatedRequest)
                    .build());

        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {
        return REGISTER_PATH.equals(path)
                || LOGIN_PATH.equals(path)
                || IDENTITY_HEALTH_PATH.equals(path)
                || "/actuator/health".equals(path);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().set(
                HttpHeaders.WWW_AUTHENTICATE, "Bearer"
        );
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
