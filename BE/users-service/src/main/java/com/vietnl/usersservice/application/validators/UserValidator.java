package com.vietnl.usersservice.application.validators;

import com.vietnl.usersservice.domain.enums.ExceptionMessage;
import com.vietnl.usersservice.infrastructure.persistence.repositories.UserRepository;
import com.vietnl.usersservice.application.requests.UserRequest;
import com.vietnl.usersservice.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateCreate(UserRequest request) {
        if (!StringUtils.hasText(request.getUsername())) throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "username"));
        if (!StringUtils.hasText(request.getPassword())) throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "password"));
        if (!StringUtils.hasText(request.getFullName())) throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "fullName"));
        if (!StringUtils.hasText(request.getEmail())) throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "email"));
        if (!StringUtils.hasText(request.getPhoneNumber())) throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "phoneNumber"));
        if (!StringUtils.hasText(request.getCitizenPid())) throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "citizenPid"));
        if (request.getBirthday() == null) throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "birthday"));

        if (request.getUsername() != null && userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException(ExceptionMessage.USER_DUPLICATE.getMessage());
        }

        if (request.getCitizenPid() != null) {
            int len = request.getCitizenPid().length();
            if (len < 9 || len > 12) {
                throw new RuntimeException(ExceptionMessage.INVALID_CITIZEN_PID.getMessage());
            }
        }

        if (request.getPassword() != null) {
            validatePassword(request.getPassword());
        }
    }

    public void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "password"));
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).+$")) {
            throw new RuntimeException(ExceptionMessage.WEAK_PASSWORD.getMessage());
        }
    }

    public User validateLogin(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        if (!password.equals(user.getPassword())) {
            throw new RuntimeException(ExceptionMessage.INVALID_PASSWORD.getMessage());
        }
        return user;
    }

    public User validateExists(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
    }
}
