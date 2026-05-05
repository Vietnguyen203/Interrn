package com.vietnl.catalogservice.domain.enums;

public enum ItemStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final Integer value;

    ItemStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
