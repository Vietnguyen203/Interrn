package com.food.order.data.repository

import com.food.order.data.ApiService
import com.food.order.data.RetrofitClient
import com.food.order.data.model.ApiResponse
import com.food.order.data.response.FoodStatisticsResponse

object StatisticsRepository {

    private val api: ApiService
        get() = RetrofitClient.instance

    suspend fun getFoodDistribution(
        token: String,
        server: String? = null,
        type: String = "day",
        date: String? = null
    ): ApiResponse<FoodStatisticsResponse> {
        return api.getFoodDistribution(token, server, type, date)
    }
}
