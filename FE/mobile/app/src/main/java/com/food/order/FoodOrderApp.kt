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

        // Áp dụng Dark Mode ngay khi App khởi động
        val darkMode = com.food.order.data.SessionManager.isDarkMode(this)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (darkMode) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
