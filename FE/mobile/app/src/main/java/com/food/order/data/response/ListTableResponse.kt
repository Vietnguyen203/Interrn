package com.food.order.data.response

import com.google.gson.annotations.SerializedName

/** Meta cho phân trang (tuỳ API có trả hay không). */
data class ListTableResponse(
    val code: Int,
    val message: String?,
    val data: List<TableResponse>?,
) {
    val isSuccess: Boolean get() = code == 0
}