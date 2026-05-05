package com.vietnl.catalogservice.application.requests;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class MenuItemRequest {
    private UUID categoryId;
    private String code;
    private String foodName;
    private BigDecimal price;
    private String imageUrl;
}
