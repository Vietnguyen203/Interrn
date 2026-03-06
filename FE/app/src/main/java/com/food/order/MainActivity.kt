package com.food.order

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.food.order.databinding.ActivityMainBinding
import com.food.order.ui.settings.ServerAddressDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Clear token on fresh launch to enforce re-login after closing app
        if (savedInstanceState == null) {
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit {
                remove("token")
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Áp padding theo system bars cho root
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // Hiện dialog nhập server ở lần chạy đầu tiên
        maybeAskServerOnce()
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
        // Cần có res/menu/main_menu.xml
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
