package com.vietnl.orderservice.application.requests;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateOrderItemRequest {

    @Min(value = 1, message = "Số lượng phải >= 1")
    private int quantity;

    private String note;
}
