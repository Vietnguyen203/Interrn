package com.food.order.data.request

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("tableId")
    val tableId: String,

    @SerializedName("items")
    val items: List<AddOrderItemRequest> = emptyList()
)
