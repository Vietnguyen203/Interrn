package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class ListFoodResponse(
    @SerializedName("code")    val code: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("data")    val data: List<FoodResponse>?,
) {
    val isSuccess: Boolean get() = code == "OK" || code == "0"
}
