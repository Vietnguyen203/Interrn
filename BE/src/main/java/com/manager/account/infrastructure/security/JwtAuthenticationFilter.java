package com.manager.account.infrastructure.security;

import com.manager.account.infrastructure.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            final String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(jwt, username)) {
                    Claims claims = jwtUtil.extractAllClaims(jwt);

                    // Set claims attribute for controllers (replaces redundant JwtFilter)
                    request.setAttribute("claims", claims);

                    String role = (String) claims.get("role");

                    if (role == null)
                        role = "USER";

                    // Spring Security expects roles to have "ROLE_" prefix for hasRole()
                    // If your check in SecurityConfig uses hasRole('ADMIN'), it checks for
                    // "ROLE_ADMIN"
                    String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                    List<SimpleGrantedAuthority> authorities = Collections
                            .singletonList(new SimpleGrantedAuthority(authority));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log error or let EntryPoint handle it
        }

        filterChain.doFilter(request, response);
    }
}
