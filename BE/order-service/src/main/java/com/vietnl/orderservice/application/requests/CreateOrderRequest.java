package com.vietnl.orderservice.application.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    private String tableId;
    private String tableNumber;
    private String note;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 món")
    @Valid
    private List<OrderItemRequest> items;
}
