//package com.food.order.data.response
//
//import com.food.order.data.model.Order
//
//data class ListOrderResponse(
//    val code: Int,
//    val message: String,
//    val data: List<Order>
//)
package com.food.order.data.response

import com.food.order.data.model.Order
import com.food.order.data.model.PageMeta
import com.google.gson.annotations.SerializedName

data class ListOrderResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Order>,
    @SerializedName("page") val page: PageMeta? = null
)
