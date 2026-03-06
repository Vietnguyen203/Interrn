// Toasts.kt
package com.food.order.common

import android.content.Context
import android.os.SystemClock
import android.widget.Toast

object Toasts {
    private var toast: Toast? = null
    private var lastMsg: String? = null
    private var lastShownAt = 0L

    @JvmStatic
    fun show(context: Context, msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        val now = SystemClock.elapsedRealtime()
        // chặn lặp lại cùng thông điệp trong ~1.5s
        if (msg.toString() == lastMsg && now - lastShownAt < 1500) return

        toast?.cancel()
        toast = Toast.makeText(context.applicationContext, msg, duration).also { it.show() }
        lastMsg = msg.toString()
        lastShownAt = now
    }
}
