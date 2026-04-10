package com.manager.statistics.infrastructure.clients.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {
    private String orderItemId;
    private String orderId;
    private String foodId;
    private String foodName;
    private String foodImage;
    private String unit;
    private double price;
    private int quantity;
    private String note;
    private String status;
    private String server;
}
