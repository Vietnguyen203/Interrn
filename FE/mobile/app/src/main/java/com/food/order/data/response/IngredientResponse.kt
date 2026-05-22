package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class IngredientResponse(
    @SerializedName("id")            val id: String,
    @SerializedName("name")          val name: String,
    @SerializedName("unit")          val unit: String,
    @SerializedName("currentStock")  val currentStock: Double,
    @SerializedName("minStockLevel") val minStockLevel: Double,
    @SerializedName("costPerUnit")   val costPerUnit: Double,
    @SerializedName("status")        val status: String
)
