package com.manager.order.domain.models.entities;

import com.manager.order.domain.models.enums.OrderStatus;
import com.manager.order.interfaces.rest.dto.response.OrderWithCreatorDTO;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@javax.persistence.Table(name = "ORDERS")
@Getter
@Setter
public class Order {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "TABLE_ID")
    private String tableId;

    @Column(name = "WAITER_ID")
    private String waiterId;

    @Column(name = "SERVER")
    private String server;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "TOTAL_AMOUNT")
    private double totalAmount;

    public OrderWithCreatorDTO convert() {
        OrderWithCreatorDTO dto = new OrderWithCreatorDTO();
        dto.setId(id);
        dto.setTableId(tableId);
        dto.setServer(server);
        dto.setCreatedAt(createdAt);
        dto.setCreatedBy(createdBy);
        dto.setStatus(status);
        dto.setItems(items);
        dto.setTotalAmount(totalAmount);
        return dto;
    }
}
