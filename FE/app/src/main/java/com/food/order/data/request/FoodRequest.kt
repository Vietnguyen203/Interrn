package com.food.order.data.request

data class FoodRequest(
    val foodName: String,
    val price: Double,
    val unit: String,
    val category: String,
    val image: String,
    val description: String? = null
)
