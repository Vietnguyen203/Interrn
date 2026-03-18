package com.manager.account.infrastructure.security;

import com.manager.account.domain.models.entities.Users;
import com.manager.account.infrastructure.persistence.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ServerAwareAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String employeeId = authentication.getName(); // bạn truyền employeeId
        String rawPassword = String.valueOf(authentication.getCredentials());
        String server = authentication.getDetails() != null ? authentication.getDetails().toString() : null;

        if (server == null || server.isBlank()) {
            throw new BadCredentialsException("Server missing");
        }

        Users u = userRepository.findByEmployeeIdAndServer(employeeId, server)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!passwordEncoder.matches(rawPassword, u.getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }

        String role = (u.getRole() == null || u.getRole().isBlank()) ? "USER" : u.getRole().toUpperCase();
        if (role.startsWith("ROLE_"))
            role = role.substring(5);

        return new UsernamePasswordAuthenticationToken(
                u.getEmployeeId(), // principal
                null, // no credentials after auth
                List.of(new SimpleGrantedAuthority("ROLE_" + role)) // authorities
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
