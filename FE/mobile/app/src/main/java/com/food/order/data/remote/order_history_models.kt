package com.food.order.data.remote



data class PagedOrders(
    val orders: List<OrderRow> // nếu BE trả data=List<OrderWithCreatorDTO> thì dùng List<OrderRow> trực tiếp
)

// match với OrderWithCreatorDTO từ BE
data class OrderRow(
    val id: String,
    val tableId: String?,
    val tableName: String?,
    val employeeName: String?,
    val status: String,
    val totalAmount: Double,
    val createdAt: String // ISO instant hoặc "yyyy-MM-ddTHH:mm:ss"
)
