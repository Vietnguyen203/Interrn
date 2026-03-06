package com.food.order.data

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

object ApiError {
    /**
     * Trả về thông báo lỗi dễ đọc từ Throwable:
     * - Ưu tiên message trong body JSON của BE (các key phổ biến: message/error/detail/title)
     * - Có phân biệt lỗi mạng (IOException) và lỗi HTTP (HttpException)
     */
    fun parse(e: Throwable): String {
        return when (e) {
            is HttpException -> {
                val code = e.code()
                val raw = e.response()?.errorBody()?.string().orEmpty()
                val msgFromBody = try {
                    if (raw.isNotBlank()) {
                        val json = JSONObject(raw)
                        listOf("message", "error", "detail", "title")
                            .firstNotNullOfOrNull { key ->
                                json.optString(key).takeIf { it.isNotBlank() }
                            }
                    } else null
                } catch (_: Exception) {
                    null
                }
                msgFromBody ?: "Request failed ($code)"
            }
            is IOException -> {
                "Network error: ${e.message ?: "please check your connection"}"
            }
            else -> {
                e.message ?: "Unknown error"
            }
        }
    }
}
