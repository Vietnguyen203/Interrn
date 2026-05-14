package com.food.order.data.mapper

import com.food.order.data.model.FoodModel
import com.food.order.data.response.MenuItemResponse

/**
 * Map MenuItemResponse (catalog-service) → FoodModel (dùng lại trong FoodAdapter/FoodFragment).
 * Các trường không còn trong catalog-service (unit, createdBy, server) được gán giá trị mặc định.
 */
fun MenuItemResponse.toFoodModel(): FoodModel {
    return FoodModel(
        id          = id,
        foodName    = foodName,
        image       = imageUrl ?: "",   // catalog dùng imageUrl thay vì image
        price       = price,
        unit        = "đ",              // catalog không còn trường unit
        category    = categoryId,       // dùng categoryId làm identifier
        createdBy   = "",
        createdAt   = createdAt ?: "",
        server      = "",
        description = null
    )
}
