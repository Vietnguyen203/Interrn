package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class UpdateOrderItemRequest(
    val foodId: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("note")
    val note: String? = null
)
