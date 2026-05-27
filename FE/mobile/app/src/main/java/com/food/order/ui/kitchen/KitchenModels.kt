package com.food.order.ui.kitchen

data class KitchenItem(
    val orderItemId: String,
    val foodName: String,
    val quantity: Int,
    val note: String?,
    val kitchenStatus: String,
    val orderId: String,
    val tableNumber: String
)

data class KitchenTableGroup(
    val tableNumber: String,
    val items: List<KitchenItem>
)
