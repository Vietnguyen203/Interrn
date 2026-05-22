package com.vietnl.catalogservice.application.requests;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class StockTransactionRequest {
    private UUID ingredientId;
    private BigDecimal quantity;
    private BigDecimal price;
    private String reason;
}
