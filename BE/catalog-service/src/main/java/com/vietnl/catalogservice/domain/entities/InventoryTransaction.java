package com.vietnl.catalogservice.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // "IMPORT", "EXPORT_SALE", "EXPORT_WASTE"

    @Column(name = "quantity", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
