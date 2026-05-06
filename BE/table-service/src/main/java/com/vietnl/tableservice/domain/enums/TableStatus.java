package com.vietnl.tableservice.domain.enums;

public enum TableStatus {
    AVAILABLE(0),
    OCCUPIED(1),
    RESERVED(2),
    CLEANING(3);

    private final Integer value;

    TableStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
