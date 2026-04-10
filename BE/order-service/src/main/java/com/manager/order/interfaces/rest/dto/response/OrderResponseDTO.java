package com.manager.order.interfaces.rest.dto.response;

import com.manager.common.domain.models.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private String id;
    private String tableId;
    private String server;
    private LocalDateTime createdAt;
    private String createdBy;
    private OrderStatus status;
    private double totalAmount;
    private String waiterId;
    private List<OrderItemResponseDTO> items;
}
