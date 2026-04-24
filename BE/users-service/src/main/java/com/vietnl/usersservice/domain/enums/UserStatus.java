package com.vietnl.usersservice.domain.enums;

public enum UserStatus {
    INACTIVE(0),
    ACTIVE(1),
    TEMPORARILY(2);

    private final Integer value;

    UserStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
