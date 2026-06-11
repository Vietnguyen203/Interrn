package com.food.order.data.request

data class IngredientRequest(
    val name: String,
    val unit: String,
    val minStock: Double
)
