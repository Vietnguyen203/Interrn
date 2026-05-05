package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("id")          val id: String,
    @SerializedName("code")        val code: String,
    @SerializedName("name")        val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("status")      val status: String?
)
