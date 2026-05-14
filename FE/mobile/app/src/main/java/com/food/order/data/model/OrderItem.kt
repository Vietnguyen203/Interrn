package com.food.order.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OrderItem(
    @SerializedName("menuItemId") val foodId: String? = null,
    @SerializedName("foodName") val foodName: String? = null,
    @SerializedName("foodImage") val foodImage: String? = null,
    @SerializedName("unitPrice") val price: Double? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("kitchenStatus") val kitchenStatus: String? = null
) : Serializable
