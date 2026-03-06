package com.food.order.ui.food

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.model.FoodModel
import com.food.order.data.model.ApiResponse
import com.food.order.data.repository.FoodRepository
import com.food.order.data.repository.OrderRepository
import com.food.order.data.repository.FileRepository
import com.food.order.data.request.AddOrderItemRequest
import com.food.order.data.request.FoodRequest
import com.food.order.data.response.FoodResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import com.food.order.data.ApiError

class FoodViewModel : ViewModel() {

    private val fileRepository = FileRepository
    private val repository = FoodRepository
    private val orderRepository = OrderRepository

    private val _imageUriFlow = MutableSharedFlow<Uri?>(replay = 0)
    val imageUriFlow = _imageUriFlow.asSharedFlow()

    private val _loadingFlow = MutableSharedFlow<Boolean>(replay = 0)
    val loadingFlow = _loadingFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>(replay = 0)
    val errorFlow = _errorFlow.asSharedFlow()

    private val _foodsFlow = MutableSharedFlow<List<FoodResponse>>(replay = 0)
    val foodsFlow = _foodsFlow.asSharedFlow()

    private val _insertFlow = MutableSharedFlow<Boolean>()
    val insertFlow = _insertFlow.asSharedFlow()

    private val _foodFlow = MutableSharedFlow<FoodModel?>()
    val foodFlow = _foodFlow.asSharedFlow()

    private val _updateFlow = MutableSharedFlow<Boolean>()
    val updateFlow = _updateFlow.asSharedFlow()

    private val _deleteFlow = MutableSharedFlow<Boolean>()
    val deleteFlow = _deleteFlow.asSharedFlow()

    private val _addOrderItemFlow = MutableSharedFlow<Boolean>()
    val addOrderItemFlow = _addOrderItemFlow.asSharedFlow()

    // ✅ Flow xoá 1 món khỏi order
    private val _deleteOrderItemFlow = MutableSharedFlow<Boolean>()
    val deleteOrderItemFlow = _deleteOrderItemFlow.asSharedFlow()

    var imageUri: Uri? = null
        private set

    fun setImageUri(uri: Uri) {
        imageUri = uri
        viewModelScope.launch { _imageUriFlow.emit(imageUri) }
    }

    fun createFood(context: Context, token: String, request: FoodRequest) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val filePart: MultipartBody.Part? = imageUri?.let { prepareFilePart(context, it) }
                if (filePart != null) {
                    val fileResponse = fileRepository.uploadImage(token, filePart)
                    if (fileResponse.isSuccess) {
                        val newRequest = request.copy(image = fileResponse.data?.url ?: "")
                        val response = repository.createFood(token, newRequest)
                        if (response.isSuccess) _insertFlow.emit(true) else _errorFlow.emit(response.message ?: "Create failed")
                    } else _errorFlow.emit("Failed to upload image")
                } else _errorFlow.emit("No image selected")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun updateFood(context: Context, token: String, request: FoodRequest) {
        viewModelScope.launch {
            if (editFood == null) { _errorFlow.emit("Food is null"); return@launch }
            _loadingFlow.emit(true)
            try {
                if (imageUri != null) {
                    val filePart: MultipartBody.Part? = prepareFilePart(context, imageUri!!)
                    if (filePart != null) {
                        val fileResponse = fileRepository.uploadImage(token, filePart)
                        if (fileResponse.isSuccess) {
                            val newRequest = request.copy(image = fileResponse.data?.url ?: "")
                            val response = repository.updateFood(token, editFood!!.id, newRequest)
                            if (response.isSuccess) _updateFlow.emit(true) else _errorFlow.emit(response.message ?: "Update failed")
                        } else _errorFlow.emit("Failed to upload image")
                    } else _errorFlow.emit("No image selected")
                } else {
                    val response = repository.updateFood(token, editFood!!.id, request)
                    if (response.isSuccess) _updateFlow.emit(true) else _errorFlow.emit(response.message ?: "Update failed")
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun getFoodsFromServer(token: String) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = repository.getFoodsFromServer(token)
                if (response.isSuccess) _foodsFlow.emit(response.data ?: emptyList()) else _errorFlow.emit(response.message ?: "Load failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    private fun prepareFilePart(context: Context, uri: Uri): MultipartBody.Part? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val inputData: ByteArray? = inputStream?.let { getBytes(it) }
            val requestBody: RequestBody? = if (mimeType != null && inputData != null) {
                inputData.toRequestBody(mimeType.toMediaTypeOrNull(), 0, inputData.size)
            } else null
            val fileName = getFileName(context, uri)
            if (requestBody != null) MultipartBody.Part.createFormData("file", fileName, requestBody) else null
        } catch (_: IOException) { null }
    }

    @Throws(IOException::class)
    private fun getBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        while ((inputStream.read(buffer).also { len = it }) != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if ("content" == uri.scheme) {
            context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) result = uri.lastPathSegment
        return result
    }

    var editFood: FoodModel? = null
        private set

    @Suppress("DEPRECATION")
    fun setArgument(bundle: Bundle?) {
        viewModelScope.launch {
            bundle?.let {
                if (it.containsKey("edit_food")) {
                    editFood = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getSerializable("edit_food", FoodModel::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        it.getSerializable("edit_food") as FoodModel
                    }
                    _foodFlow.emit(editFood!!)
                }
            }
        }
    }

    fun deleteFood(token: String) {
        viewModelScope.launch {
            if (editFood == null) {
                _errorFlow.emit("Food is null")
                return@launch
            }
            _loadingFlow.emit(true)
            try {
                val response = repository.deleteFood(token, editFood!!.id)
                _deleteFlow.emit(response.isSuccess)
                if (!response.isSuccess) _errorFlow.emit(response.message ?: "Delete failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.emit(false)
            }
        }
    }

    fun addOrderItem(token: String, orderId: String, food: FoodModel) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.addOrderItem(
                    token, orderId,
                    AddOrderItemRequest(
                        foodId = food.id,
                        foodImage = food.image,
                        foodName = food.foodName,
                        unit = food.unit,
                        price = food.price,
                        quantity = 1,
                        note = ""
                    )
                )
                if (response.isSuccess) _addOrderItemFlow.emit(true) else _errorFlow.emit(response.message ?: "Add failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    // ✅ Xoá 1 món trong order theo foodId (đúng với API removeItemFromOrder)
    fun deleteOrderItemByFoodId(token: String, orderId: String, foodId: String) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val resp = orderRepository.removeItemFromOrder(token, orderId, foodId)
                _deleteOrderItemFlow.emit(resp.isSuccess)
                if (!resp.isSuccess) _errorFlow.emit(resp.message ?: "Remove failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
                _deleteOrderItemFlow.emit(false)
            } finally {
                _loadingFlow.emit(false)
            }
        }
    }
}
