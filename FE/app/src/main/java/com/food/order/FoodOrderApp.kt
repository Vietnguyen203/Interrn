package com.food.order

import android.app.Application
import com.food.order.data.RetrofitClient

class FoodOrderApp : Application() {

    companion object {
        lateinit var instance: FoodOrderApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Khởi tạo Retrofit + sửa cấu hình cũ lưu sai (vd: "server-1" làm host)
        RetrofitClient.init(this)
        RetrofitClient.sanitizeSavedServerConfig(this)
        RetrofitClient.rebuild(this)
    }
}
