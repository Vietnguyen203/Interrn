// ✅ CHANGED: snake_case
package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class UpdateOrderItemRequest(
    @SerializedName("food_id")
    val foodId: String,
    val quantity: Int,
    val note: String? = null
)
