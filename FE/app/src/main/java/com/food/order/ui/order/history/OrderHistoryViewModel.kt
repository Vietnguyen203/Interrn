package com.food.order.ui.order.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.order.data.remote.OrderRow
import com.food.order.data.model.PageMeta          // ✅ Dùng PageMeta ở package model
import com.food.order.data.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderHistoryViewModel(
    private val repo: OrderRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<OrderRow>>(emptyList())
    val items: StateFlow<List<OrderRow>> = _items.asStateFlow()

    private val _pageMeta = MutableStateFlow<PageMeta?>(null)   // ✅ kiểu từ model
    val pageMeta: StateFlow<PageMeta?> = _pageMeta.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var reachedEnd = false
    private var token: String = ""

    fun init(token: String) {
        if (this.token.isEmpty()) {
            this.token = token
            refresh()
        }
    }

    fun refresh() {
        currentPage = 0
        reachedEnd = false
        _items.value = emptyList()
        loadNext()
    }

    fun loadNext() {
        if (_loading.value || reachedEnd) return
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.getOrderHistory(token, currentPage, pageSize)
                    .collect { pair: Pair<List<OrderRow>, PageMeta> ->
                        val list = pair.first
                        val meta = pair.second
                        if (currentPage == 0) _items.value = list
                        else _items.value = _items.value + list

                        _pageMeta.value = meta
                        reachedEnd = meta.let { it.number + 1 >= it.totalPages }
                        if (!reachedEnd) currentPage += 1
                    }
            } catch (_: Exception) {
                // TODO: expose error nếu cần
            } finally {
                _loading.value = false
            }
        }
    }
}
