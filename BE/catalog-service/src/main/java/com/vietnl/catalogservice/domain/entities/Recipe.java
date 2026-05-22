package com.vietnl.catalogservice.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "menu_item_id", nullable = false)
    private UUID menuItemId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "quantity_needed", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantityNeeded;
}
