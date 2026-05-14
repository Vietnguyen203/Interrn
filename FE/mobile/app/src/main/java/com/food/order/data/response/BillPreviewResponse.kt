package com.food.order.data.response

import com.food.order.data.model.BillPreview

data class BillPreviewResponse(
    val code: Int,
    val message: String,
    val data: BillPreview
)
