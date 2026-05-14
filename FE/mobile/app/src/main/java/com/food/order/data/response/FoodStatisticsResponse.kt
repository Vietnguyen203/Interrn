package com.food.order.data.response

import com.google.gson.annotations.SerializedName

data class PieChartItem(
    @SerializedName("name") val name: String,
    @SerializedName("count") val count: Int,
    @SerializedName("percentage") val percentage: Double
)

data class FoodStatisticsResponse(
    @SerializedName("total_orders") val totalOrders: Long,
    @SerializedName("total_items_ordered") val totalItemsOrdered: Int,
    @SerializedName("items") val items: List<PieChartItem>
)
