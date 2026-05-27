package com.food.order.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.ApiError
import com.food.order.data.repository.NotificationRepository
import com.food.order.data.response.NotificationResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val repository = NotificationRepository

    private val _notificationsFlow = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notificationsFlow = _notificationsFlow.asStateFlow()

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow.asStateFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    val unreadCount: Int get() = _notificationsFlow.value.count { !it.read }

    fun fetchNotifications(token: String, role: String = "KITCHEN") {
        viewModelScope.launch {
            _loadingFlow.value = true
            try {
                val response = repository.getRecent(token, role)
                if (response.isSuccess) {
                    _notificationsFlow.value = response.data ?: emptyList()
                } else {
                    _errorFlow.emit(response.message ?: "Không tải được thông báo")
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun markAsRead(token: String, notificationId: String) {
        viewModelScope.launch {
            try {
                val response = repository.markRead(token, notificationId)
                if (response.isSuccess) {
                    // Cập nhật local state để UI phản hồi ngay
                    _notificationsFlow.value = _notificationsFlow.value.map { n ->
                        if (n.id == notificationId) n.copy(read = true) else n
                    }
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            }
        }
    }
}