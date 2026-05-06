package com.vietnl.paymentservice.domain.models.enums;

public enum PaymentStatus implements IEnum {
    PENDING((byte) 0),
    COMPLETED((byte) 1),
    FAILED((byte) 2);

    private final byte value;
    PaymentStatus(byte value) { this.value = value; }
    @Override public byte getValue() { return value; }
}
