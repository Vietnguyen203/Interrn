package com.vietnl.paymentservice.application.dto.request;

import com.vietnl.paymentservice.domain.models.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class CreatePaymentRequest {

    @NotNull(message = "Mã đơn hàng không được để trống")
    private UUID orderId;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền thanh toán phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod method;

    private String note;

    // Getters and Setters
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
