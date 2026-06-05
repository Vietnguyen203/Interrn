package com.food.order.ui.order

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.ApiError
import com.food.order.data.mapper.toTableModel
import com.food.order.data.model.ApiResponse
import com.food.order.data.model.PageMeta
import com.food.order.data.model.Order
import com.food.order.data.model.Receipt
import com.food.order.data.model.TableModel
import com.food.order.data.repository.OrderRepository
import com.food.order.data.repository.TableRepository
import com.food.order.data.request.CheckoutRequest
import com.food.order.data.request.CopyItemsRequest
import com.food.order.data.response.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class OrderTableViewModel : ViewModel() {

    private val tableRepository = TableRepository
    private val orderRepository = OrderRepository

    private val _loadingFlow = MutableSharedFlow<Boolean>(replay = 0)
    val loadingFlow = _loadingFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>(replay = 0)
    val errorFlow = _errorFlow.asSharedFlow()

    private val _tableFlow = MutableSharedFlow<TableModel?>()
    val tableFlow = _tableFlow.asSharedFlow()

    private val _employeeOrderFlow = MutableSharedFlow<EmployeeOrderResponse?>()
    val employeeOrderFlow = _employeeOrderFlow.asSharedFlow()

    private val _copyOrderFlow = MutableSharedFlow<Boolean?>()
    val copyOrderFlow = _copyOrderFlow.asSharedFlow()

    private val _orderFlow = MutableSharedFlow<OrderResponse>()
    val orderFlow = _orderFlow.asSharedFlow()

    private val _cancelOrderFlow = MutableSharedFlow<Boolean>()
    val cancelOrderFlow = _cancelOrderFlow.asSharedFlow()

    private val _completedOrderFlow = MutableSharedFlow<String>()
    val completedOrderFlow = _completedOrderFlow.asSharedFlow()

    private val _listOrderFlow = MutableStateFlow<List<Order>>(emptyList())
    val listOrderFlow: MutableStateFlow<List<Order>> = _listOrderFlow

    private val _removeItemFromOrderFlow = MutableSharedFlow<Boolean>()
    val removeItemFromOrderFlow = _removeItemFromOrderFlow.asSharedFlow()

    private val _tablesFreeFlow = MutableSharedFlow<List<TableModel>>(replay = 0)
    val tablesFreeFlow = _tablesFreeFlow.asSharedFlow()

    // ====== NEW: Payment flows ======

    private val _checkoutFlow = MutableSharedFlow<ReceiptResponse>(replay = 0)
    val checkoutFlow = _checkoutFlow.asSharedFlow()

    private val _orderTotalFlow = MutableStateFlow<Double?>(null)
    val orderTotalFlow = _orderTotalFlow.asSharedFlow()

    val pageFlow = MutableStateFlow<PageMeta?>(null)
    private var currentPage = 0
    private var pageSize = 20
    private var pagingLoading = false

    var tableId: String? = null
        private set
    var orderId: String? = null
        private set

    fun setArguments(bundle: Bundle?) {
        tableId = bundle?.getString("tableId")
    }

    fun getDetailTable(token: String) {
        if (tableId == null) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = tableRepository.getTableByIdAndServer(token, tableId!!)
                if (response.isSuccess) {
                    orderId = response.data?.currentOrderId
                    _tableFlow.emit(response.data?.toTableModel() ?: error("Table data null"))

                    getOrderInfo(token)
                } else _errorFlow.emit(response.message ?: "Load failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun getCreateByOrder(token: String) {
        if (tableId == null) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = tableRepository.getCreateByOrder(token, tableId!!)
                if (response.isSuccess) _employeeOrderFlow.emit(response.data) else _errorFlow.emit(response.message ?: "Load failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    private fun getOrderInfo(token: String) {
        if (orderId == null) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.getOrder(token, orderId!!)
                if (response.isSuccess) {
                    val order = response.data ?: error("Order response null")
                    _orderFlow.emit(order)
                    _orderTotalFlow.value = order.totalAmount
                } else _errorFlow.emit(response.message ?: "Load failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun cancelOrder(token: String) {
        if (orderId.isNullOrEmpty()) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.cancelOrder(token, orderId!!)
                if (response.isSuccess) _cancelOrderFlow.emit(true) else _errorFlow.emit(response.message ?: "Cancel failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun completeOrder(token: String) {
        if (orderId.isNullOrEmpty()) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.complete(token, orderId!!)
                if (response.isSuccess) _completedOrderFlow.emit(orderId!!) else _errorFlow.emit(response.message ?: "Complete failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun listOrders(token: String, page: Int = 0, size: Int = 20, reset: Boolean = true) {
        internalLoadOrders(token, page, size, reset)
    }

    fun refresh(token: String, size: Int = 20) {
        pageSize = size
        currentPage = 0
        internalLoadOrders(token, page = 0, size = pageSize, reset = true)
    }

    fun loadNext(token: String) {
        val meta = pageFlow.value ?: return
        val next = meta.number + 1
        if (next >= meta.totalPages) return
        internalLoadOrders(token, page = next, size = meta.size, reset = false)
    }

    private fun internalLoadOrders(token: String, page: Int, size: Int, reset: Boolean) {
        if (pagingLoading) return
        pagingLoading = true
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.listOrders(token = token, page = page, size = size)
                if (response.isSuccess) {
                    pageFlow.value = response.page
                    currentPage = page
                    pageSize = size
                    val list = response.data ?: emptyList()
                    _listOrderFlow.value =
                        if (reset) list.map { it.data } else _listOrderFlow.value + list.map { it.data }
                } else _errorFlow.emit(response.message ?: "Load failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.emit(false)
                pagingLoading = false
            }
        }
    }

    fun removeItemFromOrder(token: String, foodId: String) {
        if (orderId.isNullOrEmpty()) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.removeItemFromOrder(token, orderId!!, foodId)
                if (response.isSuccess) _removeItemFromOrderFlow.emit(true) else _errorFlow.emit(response.message ?: "Remove failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun getTablesFromServer(token: String) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = tableRepository.getTablesFreeFromServer(token)
                if (response.isSuccess) _tablesFreeFlow.emit(response.data?.map { it.toTableModel() } ?: emptyList())
                else _errorFlow.emit(response.message ?: "Load failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun copyTableOrder(token: String, targetTableId: String) {
        if (tableId == null) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val request = CopyItemsRequest(sourceTableId = tableId!!, targetTableId = targetTableId)
                val response = tableRepository.copyTableOrder(token, request)
                if (response.isSuccess) _copyOrderFlow.emit(true) else _errorFlow.emit(response.message ?: "Copy failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    // ====== NEW: Payment actions ======
    fun checkout(token: String, payload: CheckoutRequest) {
        if (orderId.isNullOrEmpty()) return
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                // 1. Tạo payment request để lưu log (như Web)
                val paymentPayload = mapOf(
                    "orderId" to orderId!!,
                    "amount" to payload.amountReceived,
                    "method" to payload.paymentMethod,
                    "note" to (payload.note ?: "")
                )
                orderRepository.createPayment(token, paymentPayload)

                // 2. Chuyển status đơn sang COMPLETED
                val res = orderRepository.complete(token, orderId!!)
                if (res.isSuccess) {
                    _checkoutFlow.emit(
                        ReceiptResponse(
                            code = "200",
                            message = "Success",
                            data = Receipt(
                                orderId = orderId!!,
                                tableId = tableId ?: "",
                                subtotal = payload.amountReceived,
                                discount = payload.discount ?: 0.0,
                                total = payload.amountReceived,
                                paymentMethod = payload.paymentMethod,
                                amountReceived = payload.amountReceived,
                                change = 0.0,
                                paidAtEpochMs = System.currentTimeMillis()
                            )
                        )
                    )
                } else {
                    _errorFlow.emit(res.message ?: "Checkout failed")
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }
}
