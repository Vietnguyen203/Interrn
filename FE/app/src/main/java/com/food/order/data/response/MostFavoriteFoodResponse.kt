package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class MostFavoriteFoodResponse(
    @SerializedName("foodId") val foodId: String?,
    @SerializedName("foodName") val foodName: String?,
    @SerializedName("count") val count: Long?
)