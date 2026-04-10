package com.manager.order.interfaces.rest.dto.response;

import com.manager.common.domain.models.enums.OrderItemStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
    private OrderItemStatus status;
    private String server;
}
