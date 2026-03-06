package com.food.order.data.model

import java.io.Serializable

data class TableModel(
    val id: String,
    val name: String,
    val currentOrderId: String?,
) : Serializable
