package com.food.order.data.repository

import com.food.order.data.ApiService
import com.food.order.data.RetrofitClient
import com.food.order.data.model.ApiResponse
import com.food.order.data.request.FoodRequest
import com.food.order.data.response.*

object FoodRepository {

    private val api: ApiService
        get() = RetrofitClient.instance

    suspend fun createFood(token: String, request: FoodRequest): ApiResponse<FoodResponse> {
        return api.createFood(token, request)
    }

    suspend fun updateFood(token: String, id: String, request: FoodRequest): ApiResponse<FoodResponse> {
        return api.updateFood(token, id, request)
    }

    suspend fun deleteFood(token: String, id: String): ApiResponse<Void> {
        return api.deleteFood(token, id)
    }

    suspend fun getFoodsFromServer(token: String): ApiResponse<List<FoodResponse>> {
        return api.getFoodsFromServer(token)
    }

    suspend fun getFoodById(token: String, id: String): ApiResponse<FoodResponse> {
        return api.getFoodById(token, id)
    }

    suspend fun getCategories(token: String): ApiResponse<List<String>> {
        return api.getCategories(token)
    }
}
