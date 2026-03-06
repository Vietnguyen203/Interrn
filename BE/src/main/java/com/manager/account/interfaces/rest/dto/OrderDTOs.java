package com.manager.account.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

public class OrderDTOs {

    @Getter
    @Setter
    public static class OrderRequest {
        private String tableId;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddOrderItemRequest {
        @JsonAlias({ "food_id", "foodId" })
        private String foodId;

        @JsonAlias({ "food_image", "foodImage" })
        private String foodImage;

        @JsonAlias({ "food_name", "foodName" })
        private String foodName;

        private String unit;
        private double price;

        @JsonAlias({ "qty", "quantity" })
        private int quantity;

        private String note;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateOrderItemRequest {
        @JsonAlias({ "food_id", "foodId" })
        private String foodId;

        @JsonAlias({ "qty", "quantity" })
        private int quantity;

        private String note;
    }

    @Data
    public static class CheckoutRequest {
        private String paymentMethod;
        private Double amountReceived;
        private Double discount;
        private String note;
    }
}
