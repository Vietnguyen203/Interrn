package com.food.order.net

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val ctx: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sp = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Lấy token đã lưu (có thể là "<jwt>" hoặc "Bearer <jwt>")
        val raw = sp.getString("token", null)
        val token = raw?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }

        val req = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", token)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(req)
    }
}
