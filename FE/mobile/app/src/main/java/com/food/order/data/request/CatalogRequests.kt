package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class CategoryRequest(
    @SerializedName("code")        val code: String,
    @SerializedName("name")        val name: String,
    @SerializedName("description") val description: String? = null
)

data class MenuItemRequest(
    @SerializedName("code")       val code: String,
    @SerializedName("foodName")   val foodName: String,
    @SerializedName("price")      val price: Double,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("imageUrl")   val imageUrl: String? = null
)
