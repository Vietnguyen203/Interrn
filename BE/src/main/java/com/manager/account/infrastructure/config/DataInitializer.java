package com.manager.account.infrastructure.config;

import com.manager.account.domain.models.entities.Users;
import com.manager.account.infrastructure.persistence.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("Creating default admin account...");
            Users admin = new Users();
            admin.setId(UUID.randomUUID().toString());
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            admin.setFullName("System Administrator");
            admin.setEmployeeId("ADMIN001");
            admin.setServer("server-1");

            userRepository.save(admin);
            log.info("Default admin account created successfully.");
        } else {
            log.info("Admin account already exists.");
        }
    }
}
