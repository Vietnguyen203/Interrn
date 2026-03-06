package com.food.order.data.repository

import com.food.order.data.ApiService
import com.food.order.data.RetrofitClient
import com.food.order.data.model.ApiResponse
import com.food.order.data.request.CopyItemsRequest
import com.food.order.data.request.TableRequest
import com.food.order.data.response.*

object TableRepository {

    private val api: ApiService
        get() = RetrofitClient.instance

    suspend fun createTable(token: String, request: TableRequest): ApiResponse<TableResponse> =
        api.createTable(token, request)

    suspend fun updateTable(token: String, id: String, request: TableRequest): ApiResponse<TableResponse> =
        api.updateTable(token, id, request)

    suspend fun deleteTable(token: String, id: String): ApiResponse<Void> =
        api.deleteTable(token, id)

    suspend fun getTablesFromServer(token: String): ApiResponse<List<TableResponse>> =
        api.getTablesFromServer(token)

    suspend fun getTablesFreeFromServer(token: String): ApiResponse<List<TableResponse>> =
        api.getTablesFreeFromServer(token)

    suspend fun getTableByIdAndServer(token: String, id: String): ApiResponse<TableInfoResponse> =
        api.getTableByIdAndServer(token, id)

    suspend fun getCreateByOrder(token: String, id: String): ApiResponse<EmployeeOrderResponse> =
        api.getCreateByOrder(token, id)

    suspend fun copyTableOrder(token: String, request: CopyItemsRequest): ApiResponse<Void> =
        api.copyTableOrder(token, request)
}
