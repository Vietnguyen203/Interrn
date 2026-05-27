package com.food.order.data.response

data class NotificationResponse(
    val id: String?,
    val title: String?,
    val message: String?,
    val type: String?,            // success | error | info | warning
    val recipientRole: String?,   // ALL | ADMIN | WAITER | KITCHEN
    val read: Boolean = false,
    val createdAt: String?
)
