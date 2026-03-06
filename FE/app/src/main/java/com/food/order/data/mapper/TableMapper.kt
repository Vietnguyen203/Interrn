package com.food.order.data.mapper

import com.food.order.data.model.TableModel
import com.food.order.data.response.TableResponse

// Map an toàn + ưu tiên tên thật từ BE
fun TableResponse.toTableModel(): TableModel {
    val safeId = id.orEmpty()
    // CHANGED: chỉ fallback khi thật sự không có tên
    val safeName = tableName?.takeIf { it.isNotBlank() } ?: "Table"

    return TableModel(
        id = safeId,
        name = safeName,
        currentOrderId = currentOrderId
    )
}
