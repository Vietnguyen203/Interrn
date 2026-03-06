package com.food.order.data

import android.content.Context

object TokenManager {
    private const val SP_NAME = "app_prefs"
    private const val KEY_TOKEN = "token"

    fun save(ctx: Context, raw: String) {
        val value = if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_TOKEN, value).apply()
    }

    fun get(ctx: Context): String? =
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_TOKEN).apply()
    }
}
