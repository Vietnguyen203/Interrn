package com.vietnl.catalogservice.application.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IngredientRequest {
    private String name;
    private String unit;
    private BigDecimal minStock;
}
