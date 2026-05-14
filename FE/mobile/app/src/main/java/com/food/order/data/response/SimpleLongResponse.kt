package com.food.order.data.response

data class SimpleLongResponse(
    val code: String?,
    val message: String?,
    val data: Long?
) {
    val isSuccess: Boolean get() = code == "OK" || code == "0"
}
