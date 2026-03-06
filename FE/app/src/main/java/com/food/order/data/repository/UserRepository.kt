package com.food.order.data.repository

import com.food.order.data.ApiService
import com.food.order.data.RetrofitClient
import com.food.order.data.model.ApiResponse
import com.food.order.data.request.LoginRequest
import com.food.order.data.request.RegisterRequest
import com.food.order.data.request.UpdateStaffRequest
import com.food.order.data.response.*

object UserRepository {

    private val api: ApiService
        get() = RetrofitClient.instance

    // Auth
    suspend fun login(request: LoginRequest): ApiResponse<TokenResponse> =
        api.login(request)

    // Public register (RegisterDialogFragment gọi)
    suspend fun registerPublic(body: Map<String, @JvmSuppressWildcards Any>): ApiResponse<TokenResponse> =
        api.registerPublic(body)

    // Internal register (tạo staff)
    suspend fun register(token: String, request: RegisterRequest): ApiResponse<TokenResponse> =
        api.register(token, request)

    suspend fun getInfo(token: String): ApiResponse<UserResponse> =
        api.getInfo(token)

    // Staff (JWT)
    suspend fun getUsersFromServer(token: String, server: String): ListUserResponse =
        api.getUsersFromServer(token, server)

    suspend fun updateUserFromServer(
        token: String,
        server: String,
        employeeId: String,
        request: UpdateStaffRequest
    ): SimpleResponse =
        api.updateUserFromServer(token, server, employeeId, request)

    suspend fun deleteUserFromServer(
        token: String,
        server: String,
        employeeId: String
    ): SimpleResponse =
        api.deleteUserFromServer(token, server, employeeId)

    suspend fun getCountEmployee(token: String, server: String): SimpleLongResponse =
        api.getCountEmployee(token, server)
}
