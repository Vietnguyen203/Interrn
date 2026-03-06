package com.food.order.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OrderItem(
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String?,
    @SerializedName("food_image") val foodImage: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("note") val note: String?,
    @SerializedName("unit") val unit: String?
) : Serializable
