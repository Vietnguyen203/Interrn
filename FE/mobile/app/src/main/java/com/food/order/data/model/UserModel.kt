package com.food.order.data.model

import java.io.Serializable

data class UserModel(
    val employeeId: String,
    val displayName: String,
    var role: String,
    var server: String,
    val birthday: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null
) : Serializable