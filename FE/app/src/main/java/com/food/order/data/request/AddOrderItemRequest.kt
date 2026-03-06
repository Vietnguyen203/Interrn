// ✅ CHANGED: tất cả key sang snake_case để khớp BE
package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class AddOrderItemRequest(
    @SerializedName("food_id")
    val foodId: String,

    @SerializedName("food_image")
    val foodImage: String? = null,

    @SerializedName("food_name")
    val foodName: String,

    val unit: String,          // nếu BE cho phép null, đổi thành String?
    val price: Double,         // nếu BE cho phép null, đổi thành Double?
    val quantity: Int,
    val note: String? = null
)
