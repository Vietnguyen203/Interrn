package com.manager.order.domain.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@javax.persistence.Table(name = "ORDER_ITEMS")
public class OrderItem {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "ORDER_ITEM_ID")
    @JsonProperty("orderItemId")
    private String orderItemId;

    @Column(name = "FOOD_ID")
    private String foodId;

    @Column(name = "FOOD_NAME")
    private String foodName;

    @Column(name = "PRICE")
    private double price;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "NOTE")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private com.manager.common.domain.models.enums.OrderItemStatus status = com.manager.common.domain.models.enums.OrderItemStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    @JsonIgnore
    private Order order;
}
