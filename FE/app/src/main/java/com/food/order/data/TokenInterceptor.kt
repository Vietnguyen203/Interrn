package com.food.order.data

import com.food.order.FoodOrderApp
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath

        val existingAuth = req.header("Authorization")
        if (!existingAuth.isNullOrBlank() ||
            path.endsWith("/users/login") ||
            path.endsWith("/users/register") ||
            path.endsWith("/health") ||
            path.contains("/uploads") ||
            path.contains("/swagger") ||
            path.contains("/v3/api-docs")
        ) {
            return chain.proceed(req)
        }

        val raw = SessionManager.getToken(FoodOrderApp.instance)?.trim()
        if (raw.isNullOrEmpty()) return chain.proceed(req)

        val token = if (raw.startsWith("Bearer ", true)) raw else "Bearer $raw"
        val newReq = req.newBuilder()
            .header("Authorization", token)
            .build()
        return chain.proceed(newReq)
    }
}
