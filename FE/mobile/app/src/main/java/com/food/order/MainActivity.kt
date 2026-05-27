package com.food.order

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.food.order.data.RetrofitClient
import com.food.order.data.SessionManager
import com.food.order.databinding.ActivityMainBinding
import com.food.order.ui.settings.ServerAddressDialogFragment
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = SessionManager.getLang(newBase)
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        newBase.resources.updateConfiguration(config, newBase.resources.displayMetrics)
        
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    private lateinit var binding: ActivityMainBinding

    // Launcher xin quyền POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            android.util.Log.d("MainActivity", "POST_NOTIFICATIONS granted")
            fetchAndRegisterFcmToken()
        } else {
            android.util.Log.w("MainActivity", "POST_NOTIFICATIONS denied by user")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Áp dụng ngôn ngữ trước khi super.onCreate để tải đúng tài nguyên giao diện
        val lang = SessionManager.getLang(this)
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        super.onCreate(savedInstanceState)

        // Clear token on fresh launch to enforce re-login after closing app
        if (savedInstanceState == null) {
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit {
                remove("token")
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        maybeAskServerOnce()
        requestNotificationPermissionAndRegisterToken()
    }

    /**
     * Xin quyền POST_NOTIFICATIONS (chỉ cần Android 13+).
     * Sau khi có quyền, lấy FCM token và gửi lên backend.
     */
    private fun requestNotificationPermissionAndRegisterToken() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> fetchAndRegisterFcmToken()
                else -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 trở xuống: tự động có quyền
            fetchAndRegisterFcmToken()
        }
    }

    /**
     * Lấy FCM token từ Firebase và gửi lên backend nếu đã đăng nhập.
     */
    private fun fetchAndRegisterFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("MainActivity", "FCM token fetch failed", task.exception)
                return@addOnCompleteListener
            }
            val fcmToken = task.result
            android.util.Log.d("MainActivity", "FCM token: $fcmToken")

            val authToken = SessionManager.getToken(this) ?: return@addOnCompleteListener
            val role = SessionManager.getRole(this) ?: "ALL"
            val bearerToken = "Bearer $authToken"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = RetrofitClient.instance
                    api.registerFcmToken(bearerToken, mapOf(
                        "role" to role,
                        "fcmToken" to fcmToken,
                        "platform" to "ANDROID"
                    ))
                    android.util.Log.d("MainActivity", "FCM token registered to server")
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "FCM token registration failed: ${e.message}")
                }
            }
        }
    }

    private fun maybeAskServerOnce() {
        val sp = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val shown = sp.getBoolean("server_dialog_shown", false)
        if (!shown) {
            ServerAddressDialogFragment.newInstance()
                .show(supportFragmentManager, "server_dialog")
            sp.edit { putBoolean("server_dialog_shown", true) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_server -> {
                ServerAddressDialogFragment.newInstance()
                    .show(supportFragmentManager, "server_dialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
