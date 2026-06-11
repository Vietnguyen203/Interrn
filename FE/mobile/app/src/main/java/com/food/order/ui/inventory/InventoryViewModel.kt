package com.food.order.ui.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.response.IngredientResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    private val _ingredientsFlow = MutableStateFlow<List<IngredientResponse>>(emptyList())
    val ingredientsFlow: StateFlow<List<IngredientResponse>> = _ingredientsFlow

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loadingFlow

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow

    private val _transactionsFlow = MutableStateFlow<List<com.food.order.data.response.TransactionResponse>>(emptyList())
    val transactionsFlow: StateFlow<List<com.food.order.data.response.TransactionResponse>> = _transactionsFlow

    fun getIngredients(token: String, context: android.content.Context) {
        viewModelScope.launch {
            _loadingFlow.value = true
            _errorFlow.value = null
            try {
                val api = com.food.order.data.CatalogRetrofitClient.build(context)
                val response = api.getIngredients(token)
                if (response.isSuccess) {
                    _ingredientsFlow.value = response.data ?: emptyList()
                } else {
                    _errorFlow.value = response.message ?: "Lỗi tải kho hàng"
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Lỗi tải kho hàng", e)
                _errorFlow.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun getTransactions(token: String, context: android.content.Context) {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                val api = com.food.order.data.CatalogRetrofitClient.build(context)
                val response = api.getTransactions(token)
                if (response.isSuccess) {
                    _transactionsFlow.value = response.data ?: emptyList()
                } else {
                    _errorFlow.value = response.message ?: "Lỗi tải lịch sử giao dịch"
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Lỗi tải lịch sử", e)
                _errorFlow.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun importStock(token: String, context: android.content.Context, request: com.food.order.data.request.StockTransactionRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                val api = com.food.order.data.CatalogRetrofitClient.build(context)
                val response = api.importStock(token, request)
                if (response.isSuccess) {
                    onSuccess()
                    getIngredients(token, context)
                    getTransactions(token, context)
                } else {
                    _errorFlow.value = response.message ?: "Lỗi nhập kho"
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Lỗi nhập kho", e)
                _errorFlow.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun exportStock(token: String, context: android.content.Context, request: com.food.order.data.request.StockTransactionRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                val api = com.food.order.data.CatalogRetrofitClient.build(context)
                val response = api.exportStock(token, request)
                if (response.isSuccess) {
                    onSuccess()
                    getIngredients(token, context)
                    getTransactions(token, context)
                } else {
                    _errorFlow.value = response.message ?: "Lỗi xuất kho"
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Lỗi xuất kho", e)
                _errorFlow.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun createIngredient(token: String, context: android.content.Context, request: com.food.order.data.request.IngredientRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                val api = com.food.order.data.CatalogRetrofitClient.build(context)
                val response = api.createIngredient(token, request)
                if (response.isSuccess) {
                    onSuccess()
                    getIngredients(token, context)
                } else {
                    _errorFlow.value = response.message ?: "Lỗi tạo nguyên liệu"
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Lỗi tạo nguyên liệu", e)
                _errorFlow.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _loadingFlow.value = false
            }
        }
    }
}
