package com.food.order.data

import com.food.order.data.model.UserModel

object AppConstants {
    lateinit var userModel: UserModel

    fun checkExistsUser(): Boolean {
        return ::userModel.isInitialized
    }
}