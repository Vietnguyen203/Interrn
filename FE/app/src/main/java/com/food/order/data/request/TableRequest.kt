// ✅ CHANGED: ép khóa JSON từ tableName -> table_name
package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class TableRequest(
    @SerializedName("table_name")
    val tableName: String
)
