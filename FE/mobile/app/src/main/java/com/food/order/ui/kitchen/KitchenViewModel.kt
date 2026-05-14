package com.food.order.ui.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.ApiError
import com.food.order.data.repository.OrderRepository
import com.food.order.data.response.OrderItemResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KitchenViewModel : ViewModel() {

    private val repository = OrderRepository

    private val _itemsFlow = MutableStateFlow<List<OrderItemResponse>>(emptyList())
    val itemsFlow = _itemsFlow.asStateFlow()

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow.asStateFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    fun fetchKitchenItems(token: String) {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                val response = repository.getKitchenItems(token)
                if (response.isSuccess) {
                    _itemsFlow.value = response.data ?: emptyList()
                } else {
                    _errorFlow.emit(response.message ?: "Failed to fetch items")
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun updateItemStatus(token: String, orderItemId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateKitchenItemStatus(token, orderItemId, newStatus)
                if (response.isSuccess) {
                    fetchKitchenItems(token)
                } else {
                    _errorFlow.emit(response.message ?: "Failed to update status")
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            }
        }
    }
}
