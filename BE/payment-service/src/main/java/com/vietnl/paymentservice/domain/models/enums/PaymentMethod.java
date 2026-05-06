package com.vietnl.paymentservice.domain.models.enums;

public enum PaymentMethod implements IEnum {
    CASH((byte) 0),
    TRANSFER((byte) 1);

    private final byte value;
    PaymentMethod(byte value) { this.value = value; }
    @Override public byte getValue() { return value; }
}
