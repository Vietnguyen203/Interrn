package com.food.order.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.ApiError
import com.food.order.data.SessionManager
import com.food.order.data.model.UserModel
import com.food.order.data.repository.UserRepository
import com.food.order.data.request.LoginRequest
import com.food.order.data.response.TokenResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import android.util.Log

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    // ✅ CHANGED: object -> bỏ ()
    private val repository = UserRepository
    private val ctx = app.applicationContext

    private val _userFlow = MutableSharedFlow<UserModel>(replay = 0)
    val userFlow = _userFlow.asSharedFlow()

    private val _tokenFlow = MutableSharedFlow<TokenResponse>(replay = 0)
    val tokenFlow = _tokenFlow.asSharedFlow()

    private val _loadingFlow = MutableSharedFlow<Boolean>(replay = 0)
    val loadingFlow = _loadingFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                // ✅ CHANGED: không truyền ctx nữa
                val res = repository.login(request)
                if (res.isSuccess) {
                    val raw = res.data?.token.orEmpty()
                    Log.i("TAG", raw)
                    if (raw.isBlank()) {
                        _errorFlow.emit("Login failed: empty token")
                        _loadingFlow.emit(false)
                        return@launch
                    }
                    SessionManager.saveToken(ctx, raw)
                    _tokenFlow.emit(TokenResponse(token = raw))
                    getInfo()
                } else {
                    _errorFlow.emit(res.message ?: "Login failed")
                    _loadingFlow.emit(false)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> {
                        val body = e.response()?.errorBody()?.string().orEmpty()
                        "HTTP ${e.code()} - ${e.message()}${if (body.isNotBlank()) " | $body" else ""}"
                    }
                    else -> ApiError.parse(e)
                }
                _errorFlow.emit(msg)
                _loadingFlow.emit(false)
            }
        }
    }

    private fun getInfo() {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val res = repository.getInfo("") // Interceptor tự gắn Authorization
                if (res.isSuccess) {
                    res.data?.let {
                        _userFlow.emit(it.toUserModel())
                    } ?: _errorFlow.emit("User data is null")
                } else {
                    _errorFlow.emit(res.message ?: "Failed to get info")
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> {
                        val body = e.response()?.errorBody()?.string().orEmpty()
                        "HTTP ${e.code()} - ${e.message()}${if (body.isNotBlank()) " | $body" else ""}"
                    }
                    else -> ApiError.parse(e)
                }
                _errorFlow.emit(msg)
            } finally {
                _loadingFlow.emit(false)
            }
        }
    }
}
