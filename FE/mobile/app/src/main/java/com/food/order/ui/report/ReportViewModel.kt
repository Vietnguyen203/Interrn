package com.food.order.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.model.Order
import com.food.order.data.model.ApiResponse
import com.food.order.data.repository.OrderRepository
import com.food.order.data.repository.UserRepository
import com.food.order.data.response.MostFavoriteFoodResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.food.order.data.ApiError

class ReportViewModel : ViewModel() {

    private val repository = UserRepository
    private val orderRepository = OrderRepository

    var server: String = ""
    var authToken: String = ""   // "Bearer <token>"

    private val _loadingFlow = MutableSharedFlow<Boolean>(replay = 0)
    val loadingFlow = _loadingFlow.asSharedFlow()

    private val _countEmployeeFlow = MutableSharedFlow<Long>(replay = 0)
    val countEmployeeFlow = _countEmployeeFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>(replay = 0)
    val errorFlow = _errorFlow.asSharedFlow()

    private val _mostFavoriteFoodFlow = MutableSharedFlow<MostFavoriteFoodResponse?>()
    val mostFavoriteFoodFlow = _mostFavoriteFoodFlow.asSharedFlow()

    private val _reportDataFlow = MutableSharedFlow<List<com.food.order.data.response.ReportData>>()
    val reportDataFlow = _reportDataFlow.asSharedFlow()

    private val _listOrderInTimeFlow = MutableSharedFlow<List<Order>?>()
    val listOrderInTimeFlow = _listOrderInTimeFlow.asSharedFlow()

    private var currentMonth = YearMonth.now()
    private val formatterParams = DateTimeFormatter.ofPattern("MM-yyyy", Locale.ENGLISH)

    var currentReportType = "DAY" // default to DAY like web?

    fun loadData(server: String) {
        getCountEmployee(server)
        getMostFavoriteFoodInTime(server)
        getListOrderInTime(server)
        getReports(currentReportType, server)
    }

    fun loadReportsOnly(type: String, server: String) {
        currentReportType = type
        getReports(type, server)
    }

    fun fetchDataInTime() {
        getCountEmployee(server)
        getMostFavoriteFoodInTime(server)
        getListOrderInTime(server)
        getReports(currentReportType, server)
    }

    private fun getCountEmployee(server: String) {
        viewModelScope.launch {
            try {
                val result = repository.getCountEmployee(authToken, server)
                if (result.isSuccess) {
                    _countEmployeeFlow.emit(result.data ?: 0L)
                } else {
                    _errorFlow.emit(result.message ?: "Failed to get employee count")
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            }
        }
    }

    private fun getMostFavoriteFoodInTime(server: String) {
        val time = formatterParams.format(currentMonth)
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.getMostFavoriteFood(
                    token = authToken, time = time, server = server
                )
                if (response.isSuccess) _mostFavoriteFoodFlow.emit(response.data)
                else {
                    _mostFavoriteFoodFlow.emit(null)
                    if (response.code != "404" && response.message != "Not Found") {
                        _errorFlow.emit(response.message ?: "Load failed")
                    }
                }
            } catch (e: Exception) {
                _mostFavoriteFoodFlow.emit(null)
            } finally { _loadingFlow.emit(false) }
        }
    }

    private fun getReports(type: String, server: String) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.getReports(
                    token = authToken, type = type, server = server
                )
                if (response.isSuccess) _reportDataFlow.emit(response.data ?: emptyList())
                else {
                    _reportDataFlow.emit(emptyList())
                    if (response.code != "404" && response.message != "Not Found") {
                        _errorFlow.emit(response.message ?: "Load failed")
                    }
                }
            } catch (e: Exception) {
                _reportDataFlow.emit(emptyList())
            } finally { _loadingFlow.emit(false) }
        }
    }

    private fun getListOrderInTime(server: String) {
        val time = formatterParams.format(currentMonth)
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val response = orderRepository.getListOrderInTime(
                    token = authToken, time = time, server = server
                )
                if (response.isSuccess) _listOrderInTimeFlow.emit(response.data?.map { it.data } ?: emptyList())
                else {
                    _listOrderInTimeFlow.emit(emptyList())
                    if (response.code != "404" && response.message != "Not Found") {
                        _errorFlow.emit(response.message ?: "Load failed")
                    }
                }
            } catch (e: Exception) {
                _listOrderInTimeFlow.emit(emptyList())
            } finally { _loadingFlow.emit(false) }
        }
    }
}
