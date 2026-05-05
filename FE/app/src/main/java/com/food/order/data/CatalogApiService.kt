package com.food.order.data

import com.food.order.data.model.ApiResponse
import com.food.order.data.request.CategoryRequest
import com.food.order.data.request.MenuItemRequest
import com.food.order.data.response.CategoryResponse
import com.food.order.data.response.MenuItemResponse
import retrofit2.http.*

interface CatalogApiService {

    // ===== CATEGORIES =====
    @GET("catalog-service/categories")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): ApiResponse<List<CategoryResponse>>

    @GET("catalog-service/categories/{id}")
    suspend fun getCategoryById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<CategoryResponse>

    @POST("catalog-service/categories")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @Body request: CategoryRequest
    ): ApiResponse<CategoryResponse>

    @PUT("catalog-service/categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: CategoryRequest
    ): ApiResponse<CategoryResponse>

    @DELETE("catalog-service/categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Void>

    // ===== MENU ITEMS =====
    @GET("catalog-service/items")
    suspend fun getMenuItems(
        @Header("Authorization") token: String,
        @Query("categoryId") categoryId: String? = null
    ): ApiResponse<List<MenuItemResponse>>

    @GET("catalog-service/items/{id}")
    suspend fun getMenuItemById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<MenuItemResponse>

    @POST("catalog-service/items")
    suspend fun createMenuItem(
        @Header("Authorization") token: String,
        @Body request: MenuItemRequest
    ): ApiResponse<MenuItemResponse>

    @PUT("catalog-service/items/{id}")
    suspend fun updateMenuItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: MenuItemRequest
    ): ApiResponse<MenuItemResponse>

    @DELETE("catalog-service/items/{id}")
    suspend fun deleteMenuItem(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Void>
}
