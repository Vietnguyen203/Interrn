package com.food.order.data.model

import com.google.gson.annotations.SerializedName

data class FoodDto(
    @SerializedName("id")          val id: String = "",
    @SerializedName("foodName")    val foodName: String = "",   // BE dùng camelCase khi create/update
    @SerializedName("image")       val image: String? = null,
    @SerializedName("price")       val price: Double? = null,
    @SerializedName("unit")        val unit: String? = null,
    @SerializedName("category")    val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("created_by")  val createdBy: String? = null,
    @SerializedName("created_at")  val createdAt: String? = null,
    @SerializedName("server")      val server: String? = null
)
