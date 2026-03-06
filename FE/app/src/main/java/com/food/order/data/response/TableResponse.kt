package com.food.order.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TableResponse(
    @SerializedName("id")
    val id: String? = null,

    // CHANGED: map chuẩn từ BE "table_name" -> thuộc tính Kotlin "tableName"
    @SerializedName("table_name")
    val tableName: String? = null,

    @SerializedName("current_order_id")
    val currentOrderId: String? = null
) : Serializable
