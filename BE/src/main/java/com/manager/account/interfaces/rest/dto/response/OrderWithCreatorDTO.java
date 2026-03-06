package com.manager.account.interfaces.rest.dto.response;

import com.manager.account.domain.models.entities.OrderItem;
import com.manager.account.domain.models.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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





