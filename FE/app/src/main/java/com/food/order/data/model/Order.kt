package com.food.order.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Order(
    @SerializedName("id") val id: String,
    @SerializedName("table_id") val tableId: String,
    @SerializedName("server") val server: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("status") val status: String,
    @SerializedName("items") val items: List<OrderItem> = emptyList(),
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("employee_name") val employeeName: String? = null,
    @SerializedName("table_name") val tableName: String? = null
) : Serializable
