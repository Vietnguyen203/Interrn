package com.food.order.data.response

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName(value = "token", alternate = ["accessToken", "access_token", "jwt"])
    val token: String?,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null
) {
    // No longer needs specialized getters as code/message are now Strings
}
