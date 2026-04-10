package com.manager.statistics.infrastructure.clients.dto;

import com.manager.common.domain.models.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
