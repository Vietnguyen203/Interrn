package com.manager.account.infrastructure.config;

import com.manager.account.domain.models.entities.Users;
import com.manager.account.infrastructure.persistence.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;

    @Bean
    CommandLineRunner seedAdmin(BCryptPasswordEncoder encoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                Users u = new Users();
                u.setId(java.util.UUID.randomUUID().toString());
                u.setUsername("admin");
                u.setEmployeeId("admin");
                u.setEmail("admin@example.com");
                u.setRole("ADMIN");
                u.setServer("server-1");
                u.setPassword(encoder.encode("admin123"));
                userRepository.save(u);
            }
        };
    }
}
