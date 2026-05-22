package com.vietnl.catalogservice.application.requests;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DeductStockRequest {
    private List<DeductItem> items;

    @Data
    public static class DeductItem {
        private UUID menuItemId;
        private Integer quantity;
    }
}
