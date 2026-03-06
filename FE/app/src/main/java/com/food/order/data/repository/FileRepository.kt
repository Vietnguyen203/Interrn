package com.food.order.data.repository

import com.food.order.data.ApiService
import com.food.order.data.RetrofitClient
import com.food.order.data.model.ApiResponse
import com.food.order.data.response.FileUploadResponse
import okhttp3.MultipartBody

object FileRepository {

    private val api: ApiService
        get() = RetrofitClient.instance

    // ViewModel cũ vẫn truyền token -> giữ chữ ký, nhưng bỏ qua token
    @Suppress("UNUSED_PARAMETER") // [IGNORE TOKEN]
    suspend fun uploadImage(token: String, file: MultipartBody.Part): ApiResponse<FileUploadResponse> {
        return api.uploadImage(file) // ApiService không cần token
    }
}
