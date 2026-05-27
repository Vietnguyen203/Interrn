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
}
