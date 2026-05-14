package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class MenuItemResponse(
    @SerializedName("id")          val id: String,
    @SerializedName("code")        val code: String,
    @SerializedName("foodName")    val foodName: String,
    @SerializedName("price")       val price: Double,
    @SerializedName("imageUrl")    val imageUrl: String?,
    @SerializedName("categoryId")  val categoryId: String,
    @SerializedName("status")      val status: String?,
    @SerializedName("createdAt")   val createdAt: String?,
    @SerializedName("updatedAt")   val updatedAt: String?
)
