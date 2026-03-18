package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class OrderItemResponse(
    @SerializedName("id") val id: String,
    @SerializedName("orderItemId") val orderItemId: String,
    @SerializedName("foodId") val foodId: String?,
    @SerializedName("foodName") val foodName: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("note") val note: String?,
    @SerializedName("status") val status: String
)
