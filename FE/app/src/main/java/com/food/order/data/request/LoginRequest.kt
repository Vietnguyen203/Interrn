package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username")   val username: String? = null,
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("password")   val password: String,
    @SerializedName("server")     val server: String
)
