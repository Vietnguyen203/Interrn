package com.vietnl.usersservice.domain.enums;

public enum UserRole {
    USER(0),
    ADMIN(1),
    CHEF(2);

    private final Integer value;

    UserRole(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
