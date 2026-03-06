package com.food.order.ui.table

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.ApiError
import com.food.order.data.model.ApiResponse
import com.food.order.data.mapper.toTableModel
import com.food.order.data.model.TableModel
import com.food.order.data.repository.TableRepository
import com.food.order.data.request.TableRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow // ✅ CHANGED
import kotlinx.coroutines.flow.StateFlow        // ✅ CHANGED
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow     // ✅ CHANGED
import kotlinx.coroutines.launch

class TableViewModel : ViewModel() {

    private val repository = TableRepository

    private val _loadingFlow = MutableSharedFlow<Boolean>(replay = 0)
    val loadingFlow = _loadingFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>(replay = 0)
    val errorFlow = _errorFlow.asSharedFlow()

    // ✅ CHANGED: StateFlow để không miss giá trị mới nhất
    private val _tablesFlow = MutableStateFlow<List<TableModel>>(emptyList())
    val tablesFlow: StateFlow<List<TableModel>> = _tablesFlow.asStateFlow()

    private val _insertFlow = MutableSharedFlow<Boolean>(replay = 0)
    val insertFlow = _insertFlow.asSharedFlow()

    private val _tableFlow = MutableSharedFlow<TableModel?>(replay = 0)
    val tableFlow = _tableFlow.asSharedFlow()

    private val _updateFlow = MutableSharedFlow<Boolean>(replay = 0)
    val updateFlow = _updateFlow.asSharedFlow()

    private val _deleteFlow = MutableSharedFlow<Boolean>(replay = 0)
    val deleteFlow = _deleteFlow.asSharedFlow()

    private var editTable: TableModel? = null

    fun getTablesFromServer(token: String) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val res = repository.getTablesFromServer(token)
                val list = res.data?.map { it.toTableModel() } ?: emptyList()
                if (res.isSuccess) {
                    _tablesFlow.value = list
                } else _errorFlow.emit(res.message ?: "Unknown error")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.emit(false)
            }
        }
    }

    fun createTable(token: String, request: TableRequest) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            try {
                val res = repository.createTable(token, request)
                if (res.isSuccess) {
                    _insertFlow.emit(true)
                    getTablesFromServer(token) // ✅ CHANGED: reload ngay sau khi tạo
                } else _errorFlow.emit(res.message ?: "Create failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.emit(false)
            }
        }
    }

    fun updateTable(token: String, request: TableRequest) {
        viewModelScope.launch {
            if (editTable == null) { _errorFlow.emit("Table is null"); return@launch }
            _loadingFlow.emit(true)
            try {
                val res = repository.updateTable(token, editTable!!.id, request)
                if (res.isSuccess) {
                    _updateFlow.emit(true)
                    getTablesFromServer(token) // ✅ CHANGED
                } else _errorFlow.emit(res.message ?: "Update failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.emit(false)
            }
        }
    }

    fun deleteTable(token: String) {
        viewModelScope.launch {
            if (editTable == null) { _errorFlow.emit("Table is null"); return@launch }
            _loadingFlow.emit(true)
            try {
                val res = repository.deleteTable(token, editTable!!.id)
                if (res.isSuccess) {
                    _deleteFlow.emit(true)
                    getTablesFromServer(token) // ✅ CHANGED
                } else _errorFlow.emit(res.message ?: "Delete failed")
            } catch (e: Exception) {
                _errorFlow.emit(ApiError.parse(e))
            } finally {
                _loadingFlow.emit(false)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun setArgument(bundle: Bundle?) {
        viewModelScope.launch {
            bundle?.let {
                if (it.containsKey("edit_table")) {
                    editTable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getSerializable("edit_table", TableModel::class.java)
                    } else {
                        it.getSerializable("edit_table") as TableModel
                    }
                    _tableFlow.emit(editTable)
                }
            }
        }
    }
}
