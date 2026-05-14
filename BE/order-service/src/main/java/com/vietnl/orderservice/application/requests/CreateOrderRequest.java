package com.vietnl.orderservice.application.requests;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    private String tableId;
    private String tableNumber;
    private String note;

    @Valid
    private List<OrderItemRequest> items;
}
