package com.vietnl.catalogservice.application.requests;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RecipeItemRequest {
    private UUID ingredientId;
    private BigDecimal quantityNeeded;
}
