package com.food.order.data.response

data class TransactionResponse(
    val id: String,
    val ingredientId: String,
    val transactionType: String, // "IMPORT", "EXPORT_SALE", "EXPORT_LOSS", etc.
    val quantity: Double,
    val price: Double,
    val reason: String?,
    val createdAt: String
)
