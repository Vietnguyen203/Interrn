package com.vietnl.orderservice.domain.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Tham chiếu đến catalog-service menu item
    @Column(name = "menu_item_id", nullable = false)
    private String menuItemId;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "note", length = 300)
    private String note;

    // Trạng thái chế biến: PENDING, COOKING, READY, SERVED
    @Column(name = "kitchen_status")
    private String kitchenStatus = "PENDING";

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
