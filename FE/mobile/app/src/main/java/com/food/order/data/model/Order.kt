package com.food.order.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Order(
    @SerializedName("id") val id: String,
    @SerializedName("tableId") val tableId: String? = null,
    @SerializedName("server") val server: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("items") val items: List<OrderItem> = emptyList(),
    @SerializedName("totalAmount") val totalAmount: Double? = null,
    @SerializedName("employeeName") val employeeName: String? = null,
    @SerializedName("tableNumber") val tableName: String? = null
) : Serializable
