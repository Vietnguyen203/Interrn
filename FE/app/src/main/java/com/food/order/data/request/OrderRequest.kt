// ✅ CHANGED: ép key từ tableId -> table_id
package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("table_id")
    val tableId: String
)
