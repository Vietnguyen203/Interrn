package com.manager.account.interfaces.rest.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
public class UserResponse {
    private String employeeId;
    private String displayName;
    private String role;
    private LocalDateTime createdAt;
    private String createdBy;

    public UserResponse(String employeeId, String displayName, String role, LocalDateTime createdAt, String createdBy) {
        this.employeeId = employeeId;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }
}




