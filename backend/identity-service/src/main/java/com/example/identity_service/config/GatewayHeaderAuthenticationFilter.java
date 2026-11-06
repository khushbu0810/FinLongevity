package com.example.identity_service.config;

import com.example.identity_service.entity.AppUser;
import com.example.identity_service.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Establishes the local Spring Security principal from identity headers
 * inserted by the trusted API Gateway after JWT validation.
 *
 * This filter does not parse or validate JWTs. The Gateway is the only
 * component responsible for JWT signature and expiry validation.
 */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    public GatewayHeaderAuthenticationFilter(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String email = request.getHeader("X-Authenticated-User-Email");
        String userId = request.getHeader("X-Authenticated-User-Id");

        if ((email == null && userId == null)
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (email == null || email.isBlank() || userId == null || userId.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        try {
            AppUser user = (AppUser) userDetailsService.loadUserByUsername(email);

            if (!user.getId().toString().equals(userId)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
