package com.vietnl.tableservice.domain.entities;

import com.vietnl.tableservice.domain.enums.TableStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RESTAURANT_TABLE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {

    @Id
    @Column(name = "ID", length = 36)
    private UUID id;

    @Column(name = "TABLE_NUMBER")
    private Integer tableNumber;

    @Column(name = "CAPACITY")
    private Integer capacity;

    @Column(name = "STATUS")
    private TableStatus status;

    @Column(name = "CURRENT_ORDER_ID", length = 36)
    private UUID currentOrderId;

    @Column(name = "LOCATION", length = 50)
    private String location;

    @Column(name = "AREA_REF_ID", length = 36)
    private UUID areaRefId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (id == null)
            id = UUID.randomUUID();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
