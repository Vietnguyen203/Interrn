package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class AddOrderItemRequest(
    @SerializedName("menuItemId")
    val foodId: String,

    @SerializedName("foodName")
    val foodName: String,

    @SerializedName("unitPrice")
    val price: Double,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("note")
    val note: String? = null,

    val foodImage: String? = null,
    val unit: String? = null
)
