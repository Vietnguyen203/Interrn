package com.food.order.data.request

data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val birthday: String? = null,
    val gender: String? = null,       // "MALE" | "FEMALE" | "OTHER"
    val role: String? = "USER",
    val server: String? = "HCM",
    val employeeId: String? = null
)
