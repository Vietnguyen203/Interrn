package com.food.order.data.response

data class SimpleResponse(
    val code: String?,
    val message: String?,
) {
    val isSuccess: Boolean get() = code == "OK" || code == "0"
}
