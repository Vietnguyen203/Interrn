package com.food.order.data.response

import com.google.gson.annotations.SerializedName
import com.food.order.data.model.Order

data class OrderResponse(
    val code: Int,
    val message: String,
    val data: Order
)