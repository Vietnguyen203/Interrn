package com.food.order.data.repository

import com.food.order.data.ApiService
import com.food.order.data.RetrofitClient
import com.food.order.data.model.ApiResponse
import com.food.order.data.response.NotificationResponse

object NotificationRepository {

    private val api: ApiService get() = RetrofitClient.instance

    suspend fun getRecent(token: String, role: String): ApiResponse<List<NotificationResponse>> =
        api.getRecentNotifications(token, role)

    suspend fun markRead(token: String, id: String): ApiResponse<NotificationResponse> =
        api.markNotificationRead(token, id)

    suspend fun registerFcmToken(token: String, role: String, fcmToken: String): ApiResponse<Void> =
        api.registerFcmToken(token, mapOf("role" to role, "fcmToken" to fcmToken, "platform" to "ANDROID"))

    suspend fun removeFcmToken(token: String, fcmToken: String): ApiResponse<Void> =
        api.removeFcmToken(token, mapOf("fcmToken" to fcmToken))
}
