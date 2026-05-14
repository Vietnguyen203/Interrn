package com.food.order.data.response

import com.food.order.data.model.Order
import com.food.order.data.model.OrderItem
import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("tableId") val tableId: String? = null,
    @SerializedName("tableNumber") val tableNumber: String? = null,
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("totalAmount") val totalAmount: Double? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("items") val items: List<OrderItem> = emptyList()
) {
    val data: Order
        get() = Order(
            id = id.orEmpty(),
            tableId = tableId,
            createdAt = createdAt,
            createdBy = createdBy,
            status = status,
            items = items,
            totalAmount = totalAmount,
            tableName = tableNumber
        )
}
