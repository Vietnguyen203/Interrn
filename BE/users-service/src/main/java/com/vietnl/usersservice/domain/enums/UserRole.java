package com.vietnl.usersservice.domain.enums;

public enum UserRole {
    WAITER(0),
    ADMIN(1),
    CHEF(2),
    KITCHEN(3);

    private final Integer value;

    UserRole(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
