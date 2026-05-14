package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class RevenueByWeekResponse(
    val code: String?,
    val message: String,
    val data: List<RevenueByWeek>,
)

data class RevenueByWeek(
    val week: String,
    val total: Double,
)