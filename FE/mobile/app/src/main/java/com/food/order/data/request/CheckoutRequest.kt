package com.food.order.data.request

data class CheckoutRequest(
    val paymentMethod: String,     // "CASH" | "CARD" | "QR" ...
    val amountReceived: Double,
    val discount: Double? = null,
    val note: String? = null
)
