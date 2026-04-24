package com.vietnl.usersservice.application.responses;

import com.vietnl.usersservice.domain.entities.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private Integer role;
    private Integer status;
    private String phoneNumber;
    private String fullName;
    private String email;
    private LocalDateTime birthday;
    private String citizenPid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        if (user == null)
            return null;
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setBirthday(user.getBirthday());
        response.setCitizenPid(user.getCitizenPid());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
