package com.vietnl.usersservice.adapter.apis;

import com.vietnl.usersservice.application.requests.LoginRequest;
import com.vietnl.usersservice.application.requests.ResetPasswordRequest;
import com.vietnl.usersservice.application.requests.UserRequest;
import com.vietnl.usersservice.application.responses.UserResponse;
import com.vietnl.usersservice.application.usecases.UserService;
import com.vietnl.usersservice.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users-service/request")
@RequiredArgsConstructor
public class UserAPI {

    private final UserService userService;

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String token = userService.login(
                request.getUsername(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                Map.of(
                        "code", "200",
                        "message", "Login success",
                        "token", token
                )
        );
    }

    // ===== CREATE USER =====
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserRequest request) {

        User createdUser = userService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "code", "201",
                                "message", "User created",
                                "data", UserResponse.fromEntity(createdUser)
                        )
                );
    }

    // ===== GET USER BY ID =====
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable String id) {

        User user = userService.getById(id);

        return ResponseEntity.ok(
                Map.of(
                        "code", "200",
                        "data", UserResponse.fromEntity(user)
                )
        );
    }

    // ===== GET ALL USERS =====
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll() {

        List<User> users = userService.getAll();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                Map.of(
                        "code", "200",
                        "data", responses
                )
        );
    }

    // ===== DELETE USER =====
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {

        userService.delete(id);

        return ResponseEntity.ok(
                Map.of(
                        "code", "200",
                        "message", "Deleted successfully"
                )
        );
    }

    // ===== UPDATE PASSWORD =====
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String id, @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(
                Map.of(
                        "code", "200",
                        "message", "Mật khẩu đã được thiết lập lại thành công"
                )
        );
    }
}