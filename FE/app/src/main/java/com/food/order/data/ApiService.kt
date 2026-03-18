package com.food.order.data

import com.food.order.data.model.ApiResponse
import com.food.order.data.remote.PagedOrders
import com.food.order.data.request.*
import com.food.order.data.response.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== USERS / AUTH =====
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<TokenResponse>

    // public register (dùng ở RegisterDialogFragment)
    // BE controller is /users/register
    @POST("users/register")
    suspend fun registerPublic(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse<TokenResponse>

    @POST("users/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): ApiResponse<Any>

    @POST("users/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): ApiResponse<Any>

    // register qua flow nội bộ (Admin tạo staff)
    @POST("users/register")
    suspend fun register(
        @Header("Authorization") token: String,
        @Body request: RegisterRequest
    ): ApiResponse<TokenResponse>

    @GET("users/getInfo")
    suspend fun getInfo(@Header("Authorization") token: String): ApiResponse<UserResponse>

    // ===== STAFF (JWT) =====
    @GET("users")
    suspend fun getUsersFromServer(
        @Header("Authorization") token: String,
        @Query("server") server: String
    ): ListUserResponse

    @GET("users/count-by-server")
    suspend fun getCountEmployee(
        @Header("Authorization") token: String,
        @Query("server") server: String
    ): SimpleLongResponse

    @PUT("users/{server}/{employeeId}")
    suspend fun updateUserFromServer(
        @Header("Authorization") token: String,
        @Path("server") server: String,
        @Path("employeeId") employeeId: String,
        @Body request: UpdateStaffRequest
    ): SimpleResponse

    @DELETE("users/{server}/{employeeId}")
    suspend fun deleteUserFromServer(
        @Header("Authorization") token: String,
        @Path("server") server: String,
        @Path("employeeId") employeeId: String
    ): SimpleResponse

    // ===== FILES =====
    @Multipart
    @POST("files/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ApiResponse<FileUploadResponse>

    // ===== FOODS =====
    @GET("foods/list")
    suspend fun getFoodsFromServer(@Header("Authorization") token: String): ApiResponse<List<FoodResponse>>

    @POST("foods/create")
    suspend fun createFood(
        @Header("Authorization") token: String,
        @Body request: FoodRequest
    ): ApiResponse<FoodResponse>

    @PUT("foods/{id}")
    suspend fun updateFood(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: FoodRequest
    ): ApiResponse<FoodResponse>

    @DELETE("foods/{id}")
    suspend fun deleteFood(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Void>

    @GET("foods/{id}")
    suspend fun getFoodById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<FoodResponse>

    @GET("foods/categories")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): ApiResponse<List<String>>

    // ===== TABLES =====
    @GET("tables/list")
    suspend fun getTablesFromServer(@Header("Authorization") token: String): ApiResponse<List<TableResponse>>

    @GET("tables/list-free")
    suspend fun getTablesFreeFromServer(@Header("Authorization") token: String): ApiResponse<List<TableResponse>>

    @POST("tables/create")
    suspend fun createTable(
        @Header("Authorization") token: String,
        @Body request: TableRequest
    ): ApiResponse<TableResponse>

    @PUT("tables/{id}")
    suspend fun updateTable(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: TableRequest
    ): ApiResponse<TableResponse>

    @DELETE("tables/{id}")
    suspend fun deleteTable(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Void>

    @GET("tables/{tableId}")
    suspend fun getTableByIdAndServer(
        @Header("Authorization") token: String,
        @Path("tableId") id: String
    ): ApiResponse<TableInfoResponse>

    @GET("tables/{tableId}/current-order/creator")
    suspend fun getCreateByOrder(
        @Header("Authorization") token: String,
        @Path("tableId") id: String
    ): ApiResponse<EmployeeOrderResponse>

    @POST("tables/copy-items")
    suspend fun copyTableOrder(
        @Header("Authorization") token: String,
        @Body request: CopyItemsRequest
    ): ApiResponse<Void>

    // ===== ORDERS (core) =====
    @GET("orders/{id}")
    suspend fun getOrder(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<OrderResponse>

    @GET("orders")
    suspend fun listOrders(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): ApiResponse<List<OrderResponse>>

    @POST("orders/create")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: OrderRequest
    ): ApiResponse<Map<String, String>>

    @DELETE("orders/{id}")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Void>

    @PUT("orders/{id}/complete")
    suspend fun complete(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Void>

    // ===== ORDER ITEMS =====
    @PUT("orders/{id}/add-item")
    suspend fun addOrderItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: AddOrderItemRequest
    ): ApiResponse<Void>

    @PUT("orders/{id}/update-item")
    suspend fun updateOrderItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateOrderItemRequest
    ): ApiResponse<Void>

    @DELETE("orders/{id}/remove-item/{foodId}")
    suspend fun removeItemFromOrder(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("foodId") foodId: String
    ): ApiResponse<Void>

    @PUT("orders/{id}/waiter")
    suspend fun updateWaiter(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ApiResponse<Void>

    // ===== REPORTS (JWT) =====
    @GET("orders/most-favorite-food")
    suspend fun getMostFavoriteFood(
        @Header("Authorization") token: String,
        @Query("time") time: String? = null,
        @Query("server") server: String? = null
    ): ApiResponse<MostFavoriteFoodResponse>

    @GET("orders/revenue-by-week")
    suspend fun getRevenueByWeek(
        @Header("Authorization") token: String,
        @Query("time") time: String? = null,
        @Query("server") server: String? = null
    ): ApiResponse<RevenueByWeekResponse>

    @GET("orders/list")
    suspend fun getListOrderInTime(
        @Header("Authorization") token: String,
        @Query("time") time: String? = null,
        @Query("server") server: String? = null
    ): ApiResponse<List<OrderResponse>>

    // ===== ORDER HISTORY (JWT) =====
    @GET("orders")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PagedOrders>>

    // ===== PAYMENT =====
    @POST("orders/{id}/preview-bill")
    suspend fun previewBill(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any> = emptyMap()
    ): ApiResponse<BillPreviewResponse>

    @PUT("orders/{id}/checkout")
    suspend fun checkoutOrder(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: CheckoutRequest
    ): ApiResponse<ReceiptResponse>

    // ===== STATISTICS =====
    @GET("statistics/food-distribution")
    suspend fun getFoodDistribution(
        @Header("Authorization") token: String,
        @Query("server") server: String? = null,
        @Query("type") type: String = "day",
        @Query("date") date: String? = null
    ): ApiResponse<FoodStatisticsResponse>
    // ===== KITCHEN =====
    @GET("kitchen/items")
    suspend fun getKitchenItems(@Header("Authorization") token: String): ApiResponse<List<OrderItemResponse>>

    @PUT("kitchen/items/{orderItemId}/status")
    suspend fun updateKitchenItemStatus(
        @Header("Authorization") token: String,
        @Path("orderItemId") orderItemId: String,
        @Body body: Map<String, String>
    ): ApiResponse<Void>
}
