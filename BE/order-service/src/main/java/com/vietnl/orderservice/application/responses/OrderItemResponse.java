package com.vietnl.orderservice.application.responses;

import com.vietnl.orderservice.domain.models.entities.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemResponse {

    private UUID id;
    private String menuItemId;
    private String foodName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
    private String note;
    private String kitchenStatus;

    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse r = new OrderItemResponse();
        r.setId(item.getId());
        r.setMenuItemId(item.getMenuItemId());
        r.setFoodName(item.getFoodName());
        r.setUnitPrice(item.getUnitPrice());
        r.setQuantity(item.getQuantity());
        r.setSubtotal(item.getSubtotal());
        r.setNote(item.getNote());
        r.setKitchenStatus(item.getKitchenStatus());
        return r;
    }
}
