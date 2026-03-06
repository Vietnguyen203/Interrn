package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class FoodResponse(
    @SerializedName("id")          val id: String,
    @SerializedName("food_name")   val foodName: String,
    @SerializedName("image")       val image: String?,
    @SerializedName("price")       val price: Double,
    @SerializedName("unit")        val unit: String,
    @SerializedName("category")    val category: String,
    @SerializedName("created_by")  val createdBy: String,
    @SerializedName("created_at")  val createdAt: String,
    @SerializedName("server")      val server: String,
    @SerializedName("description") val description: String?
)
