package com.vietnl.catalogservice.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        log.info("[JWT Filter] {} {} | Auth header: {}",
                request.getMethod(), request.getRequestURI(),
                authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "MISSING");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[JWT Filter] No Bearer token found — request continues unauthenticated");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);
            log.info("[JWT Filter] Extracted username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwt, username)) {
                    String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                    log.info("[JWT Filter] Raw role from JWT: {}", role);
                    if (role == null || role.isBlank()) role = "USER";
                    if (!role.startsWith("ROLE_")) role = "ROLE_" + role;
                    log.info("[JWT Filter] Final authority: {} for user: {}", role, username);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("[JWT Filter] Token INVALID for username: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("[JWT Filter] Exception parsing token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
