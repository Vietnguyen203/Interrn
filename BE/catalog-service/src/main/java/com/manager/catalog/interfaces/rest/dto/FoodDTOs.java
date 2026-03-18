package com.manager.catalog.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.manager.catalog.domain.models.enums.FoodCategory;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

public class FoodDTOs {

    @Getter
    @Setter
    public static class FoodRequest {
        @NotBlank(message = "Food name is required")
        private String foodName;

        @Positive(message = "Price must be a positive number")
        private double price;

        private String image;

        @NotBlank(message = "Unit is required")
        private String unit;

        @NotNull(message = "Category is required")
        private FoodCategory category;

        private String description;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String createdBy;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private LocalDateTime createdAt;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String server;
    }

    @Getter
    @Setter
    public static class UpdateFoodRequest {
        @NotBlank(message = "Food name is required")
        private String foodName;

        @Positive(message = "Price must be a positive number")
        private Double price;

        @NotBlank(message = "Unit is required")
        private String unit;

        private String image;

        @NotNull(message = "Category is required")
        private FoodCategory category;

        private String description;
    }
}
