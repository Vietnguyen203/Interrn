package com.food.order.data.response

import com.google.gson.annotations.SerializedName
import com.food.order.data.model.Receipt

data class ReceiptResponse(
    val code: String?,
    val message: String,
    val data: Receipt
)
