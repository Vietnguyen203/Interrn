package com.food.order.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model khớp JSON từ BE:
 * {
 *   "id": "...",
 *   "table_name": "...",
 *   "current_order_id": "...|null",
 *   "created_by": "...|null",
 *   "created_at": "...|null",
 *   "server": "...|null"
 * }
 *
 * Lưu ý: các field có thể null phải để kiểu nullable để tránh crash khi parse.
 */
data class TableDto(
    @SerializedName("id")              val id: String = "",
    @SerializedName("table_name")      val tableName: String = "",
    @SerializedName("current_order_id") val currentOrderId: String? = null,
    @SerializedName("created_by")      val createdBy: String? = null,
    @SerializedName("created_at")      val createdAt: String? = null,
    @SerializedName("server")          val server: String? = null
)
