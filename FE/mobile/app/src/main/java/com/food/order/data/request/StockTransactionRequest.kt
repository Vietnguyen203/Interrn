package com.food.order.data.request

data class StockTransactionRequest(
    val ingredientId: String,
    val quantity: Double,
    val price: Double,
    val reason: String
)
