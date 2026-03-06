package com.food.order.data.model

data class Receipt(
    val orderId: String,
    val tableId: String,
    val subtotal: Double,
    val discount: Double,
    val total: Double,
    val paymentMethod: String,
    val amountReceived: Double,
    val change: Double,
    val paidAtEpochMs: Long
)
