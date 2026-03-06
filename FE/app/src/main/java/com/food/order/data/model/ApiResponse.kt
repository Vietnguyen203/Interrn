package com.food.order.data.model

import com.google.gson.annotations.SerializedName

/**
 * Wrapper chuẩn cho mọi response từ BE.
 * Ví dụ:
 * {
 *   "code": 0,
 *   "message": "Success",
 *   "data": [...],
 *   "page": { "number":0, "size":20, "totalPages":3, "totalElements":60 }
 * }
 *
 * Lưu ý:
 * - data có thể null -> để T? để tránh crash khi parse.
 * - page là optional -> PageMeta? = null.
 */
data class ApiResponse<T>(
    @SerializedName("code")    val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data")    val data: T? = null,
    @SerializedName("page")    val page: PageMeta? = null
) {
    val isSuccess: Boolean get() = code == "OK" || code == "0"
}

/** Meta cho phân trang (tuỳ API có trả hay không). */
data class PageMeta(
    @SerializedName("number")         val number: Int = 0,
    @SerializedName("size")           val size: Int = 0,
    @SerializedName("total_pages")    val totalPages: Int = 0,
    @SerializedName("total_elements") val totalElements: Long = 0
)
