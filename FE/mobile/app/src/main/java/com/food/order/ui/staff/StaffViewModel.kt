package com.food.order.ui.staff

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.ApiError
import com.food.order.data.model.UserModel
import com.food.order.data.model.ApiResponse
import com.food.order.data.repository.UserRepository
import com.food.order.data.request.RegisterRequest
import com.food.order.data.request.UpdateStaffRequest
import com.food.order.data.response.UserResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import androidx.core.os.BundleCompat

class StaffViewModel : ViewModel() {

    private val repository = UserRepository

    private val _loadingFlow = MutableSharedFlow<Boolean>(replay = 0)
    val loadingFlow = _loadingFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val _usersFlow = MutableSharedFlow<List<UserResponse>>()
    val usersFlow = _usersFlow.asSharedFlow()

    private val _insertFlow = MutableSharedFlow<Boolean>()
    val insertFlow = _insertFlow.asSharedFlow()

    private val _updateFlow = MutableSharedFlow<Boolean>()
    val updateFlow = _updateFlow.asSharedFlow()

    private val _deleteFlow = MutableSharedFlow<Boolean>()
    val deleteFlow = _deleteFlow.asSharedFlow()

    private val _staffFlow = MutableSharedFlow<UserModel?>()
    val staffFlow = _staffFlow.asSharedFlow()

    private fun asAuthHeader(raw: String?): String? {
        val t = raw?.trim().orEmpty()
        if (t.isBlank()) return null
        return if (t.startsWith("Bearer ", ignoreCase = true)) t else "Bearer $t"
    }

    fun getUserFromServer(token: String?, server: String) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val header = asAuthHeader(token) ?: run {
                    _errorFlow.emit("Missing token. Please login again."); return@launch
                }
                val response = repository.getUsersFromServer(header, server)
                if (response.isSuccess) {
                    _usersFlow.emit(response.data ?: emptyList())
                } else {
                    _errorFlow.emit(response.message ?: "Load failed")
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun createStaff(token: String?, request: RegisterRequest) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val header = asAuthHeader(token) ?: run {
                    _errorFlow.emit("Missing token. Please login again."); return@launch
                }
                val res = repository.register(header, request)
                if (res.isSuccess) {
                    _insertFlow.emit(true)
                } else {
                    _errorFlow.emit(res.message ?: "Register failed")
                    _insertFlow.emit(false)
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e)); _insertFlow.emit(false)
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun updateStaff(token: String?, server: String, employeeId: String, request: UpdateStaffRequest) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val header = asAuthHeader(token) ?: run {
                    _errorFlow.emit("Missing token. Please login again."); return@launch
                }
                val response = repository.updateUserFromServer(header, server, employeeId, request)
                if (response.isSuccess) {
                    _updateFlow.emit(true)
                } else {
                    _errorFlow.emit(response.message ?: "Update failed")
                    _updateFlow.emit(false)
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e)); _updateFlow.emit(false)
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun deleteStaff(token: String?, server: String, employeeId: String) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val header = asAuthHeader(token) ?: run {
                    _errorFlow.emit("Missing token. Please login again."); return@launch
                }
                val response = repository.deleteUserFromServer(header, server, employeeId)
                if (response.isSuccess) {
                    _deleteFlow.emit(true)
                } else {
                    _errorFlow.emit(response.message ?: "Delete failed")
                    _deleteFlow.emit(false)
                }
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e)); _deleteFlow.emit(false)
            } finally { _loadingFlow.emit(false) }
        }
    }

    fun setArgument(bundle: Bundle?) {
        viewModelScope.launch {
            val staff: UserModel? = bundle?.let { b ->
                BundleCompat.getSerializable(b, "edit_staff", UserModel::class.java)
            }
            _staffFlow.emit(staff)
        }
    }
}
