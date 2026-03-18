package com.manager.order.interfaces.rest.dto.response;



import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.manager.order.domain.models.entities.OrderItem;
import com.manager.order.domain.models.enums.OrderStatus;

@Getter
@Setter
public class OrderWithCreatorDTO {

    private String id;
    private String tableId;
    private String tableName;
    private String server;
    private LocalDateTime createdAt;
    private String createdBy;
    private String employeeName;
    private OrderStatus status;
    private List<OrderItem> items = new ArrayList<>();
    private double totalAmount;
}





