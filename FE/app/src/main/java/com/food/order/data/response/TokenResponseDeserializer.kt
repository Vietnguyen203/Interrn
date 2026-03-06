package com.food.order.data.response

import com.google.gson.*
import java.lang.reflect.Type

class TokenResponseDeserializer : JsonDeserializer<TokenResponse> {

    private val tokenKeys = setOf("token", "accessToken", "access_token", "jwt")

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TokenResponse {
        if (json == null || json.isJsonNull) {
            return TokenResponse(token = null, code = null, message = null)
        }

        val root = json

        // Lấy code/message ở cấp root nếu có (dạng gì cũng giữ nguyên)
        val codeEl = root.asJsonObject?.get("code")
        val messageEl = root.asJsonObject?.get("message")

        // Tìm token ở mọi nơi trong JSON
        val token = findTokenRecursively(root)

        return TokenResponse(
            token = token,
            code = codeEl?.asString,
            message = messageEl?.asString
        )
    }

    private fun findTokenRecursively(el: JsonElement?): String? {
        if (el == null || el.isJsonNull) return null

        when {
            el.isJsonObject -> {
                val obj = el.asJsonObject
                // Ưu tiên key token trực tiếp
                for ((k, v) in obj.entrySet()) {
                    if (k in tokenKeys && v.isJsonPrimitive) {
                        val prim = v.asJsonPrimitive
                        if (prim.isString) {
                            val s = prim.asString?.trim().orEmpty()
                            if (s.isNotBlank()) return s
                        }
                    }
                }
                // Duyệt sâu tiếp các value
                for ((_, v) in obj.entrySet()) {
                    val found = findTokenRecursively(v)
                    if (found != null) return found
                }
            }

            el.isJsonArray -> {
                for (item in el.asJsonArray) {
                    val found = findTokenRecursively(item)
                    if (found != null) return found
                }
            }
        }
        return null
    }
}
