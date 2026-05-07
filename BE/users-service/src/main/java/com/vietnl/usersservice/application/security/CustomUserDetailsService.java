package com.vietnl.usersservice.application.security;

import com.vietnl.usersservice.domain.entities.User;
import com.vietnl.usersservice.infrastructure.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String roleName;
        switch (user.getRole() != null ? user.getRole() : 0) {
            case 1  -> roleName = "ROLE_ADMIN";
            case 2  -> roleName = "ROLE_CHEF";
            case 3  -> roleName = "ROLE_KITCHEN";
            default -> roleName = "ROLE_WAITER"; // 0 = WAITER (default)
        }
        
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));
        System.out.println(">>> [Auth Debug] User: " + username + " loaded with role: " + roleName);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
