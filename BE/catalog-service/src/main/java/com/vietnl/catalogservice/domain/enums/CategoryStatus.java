package com.vietnl.catalogservice.domain.enums;

public enum CategoryStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final Integer value;

    CategoryStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
