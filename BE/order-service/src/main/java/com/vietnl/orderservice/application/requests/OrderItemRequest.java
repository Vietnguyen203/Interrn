package com.vietnl.orderservice.application.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @NotBlank(message = "menuItemId không được trống")
    private String menuItemId;

    @NotBlank(message = "Tên món không được trống")
    private String foodName;

    @NotNull(message = "Giá không được trống")
    private BigDecimal unitPrice;

    @Min(value = 1, message = "Số lượng phải >= 1")
    private int quantity = 1;

    private String note;
}
