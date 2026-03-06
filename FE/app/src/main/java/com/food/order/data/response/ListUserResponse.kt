package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class ListUserResponse(
    val code: String?,
    val message: String?,
    val data: List<UserResponse>?,
) {
    val isSuccess: Boolean get() = code == "OK" || code == "0"
}