package com.food.order.data.request

data class CopyItemsRequest(
    val sourceTableId: String,
    val targetTableId: String
)
