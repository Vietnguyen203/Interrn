package com.vietnl.usersservice.application.usecases;

import com.vietnl.usersservice.infrastructure.persistence.repositories.UserRepository;
import com.vietnl.usersservice.application.requests.ResetPasswordRequest;
import com.vietnl.usersservice.application.requests.UserRequest;
import com.vietnl.usersservice.application.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vietnl.usersservice.application.validators.UserValidator;
import com.vietnl.usersservice.domain.entities.User;
import com.vietnl.usersservice.domain.enums.UserRole;
import com.vietnl.usersservice.domain.enums.UserStatus;
import com.vietnl.usersservice.domain.enums.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException(ExceptionMessage.INVALID_PASSWORD.getMessage());
        }

        return jwtService.generateToken(username);
    }

    public User create(UserRequest request) {
        userValidator.validateCreate(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER.getValue()); // Default: 0 (USER)
        user.setStatus(UserStatus.ACTIVE.getValue()); // Default: 1 (ACTIVE)
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setBirthday(request.getBirthday());
        user.setCitizenPid(request.getCitizenPid());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getById(String id) {
        return userValidator.validateExists(id);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void delete(String id) {
        userValidator.validateExists(id);
        userRepository.deleteById(UUID.fromString(id));
    }

    public void resetPassword(String id, ResetPasswordRequest request) {
        User user = userValidator.validateExists(id);

        userValidator.validatePassword(request.getPassword());

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
