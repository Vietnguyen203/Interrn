package com.food.order.ui.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.ApiError
import com.food.order.data.repository.OrderRepository
import com.food.order.data.response.OrderResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KitchenViewModel : ViewModel() {

    private val repository = OrderRepository

    private val _groupedItemsFlow = MutableStateFlow<List<KitchenTableGroup>>(emptyList())
    val groupedItemsFlow = _groupedItemsFlow.asStateFlow()

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow.asStateFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    fun fetchKitchenItems(token: String) {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                // Fetch both PENDING and CONFIRMED orders concurrently
                val pendingDeferred = async { repository.listOrders(token, "PENDING") }
                val confirmedDeferred = async { repository.listOrders(token, "CONFIRMED") }

                val pendingRes = pendingDeferred.await()
                val confirmedRes = confirmedDeferred.await()

                val pendingOrders = if (pendingRes.isSuccess) pendingRes.data ?: emptyList() else emptyList()
                val confirmedOrders = if (confirmedRes.isSuccess) confirmedRes.data ?: emptyList() else emptyList()

                val allKitchenItems = mutableListOf<KitchenItem>()

                val extractItems = { orders: List<OrderResponse> ->
                    orders.forEach { order ->
                        order.items.forEach { item ->
                            val ks = item.kitchenStatus ?: "PENDING"
                            if (ks == "PENDING" || ks == "COOKING") {
                                allKitchenItems.add(
                                    KitchenItem(
                                        orderItemId = item.id.orEmpty(),
                                        foodName = item.foodName ?: "Món không tên",
                                        quantity = item.quantity ?: 1,
                                        note = item.note,
                                        kitchenStatus = ks,
                                        orderId = order.id.orEmpty(),
                                        tableNumber = if (!order.tableNumber.isNullOrBlank()) {
                                            if (order.tableNumber.contains("Bàn")) order.tableNumber else "Bàn ${order.tableNumber}"
                                        } else {
                                            "Mang đi"
                                        }
                                    )
                                )
                            }
                        }
                    }
                }

                extractItems(pendingOrders)
                extractItems(confirmedOrders)

                // Group by tableNumber and sort
                val grouped = allKitchenItems.groupBy { it.tableNumber }
                    .map { (tableNum, items) -> KitchenTableGroup(tableNum, items) }
                    .sortedBy { it.tableNumber }

                _groupedItemsFlow.value = grouped
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

    fun completeAllItems(token: String, items: List<KitchenItem>) {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                // Call updateKitchenItemStatus concurrently for all items in the group
                val jobs = items.map { item ->
                    launch {
                        try {
                            repository.updateKitchenItemStatus(token, item.orderItemId, "READY")
                        } catch (e: Exception) {
                            System.err.println("Lỗi cập nhật món ${item.foodName}: ${e.message}")
                        }
                    }
                }
                jobs.forEach { it.join() }
                fetchKitchenItems(token)
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.value = false
            }
        }
    }
}
