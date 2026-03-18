package com.food.order.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_TOKEN  = "token"
    private const val KEY_ROLE   = "role"
    private const val KEY_USER_JSON = "userProfileJson"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Token
    fun saveToken(ctx: Context, token: String) {
        prefs(ctx).edit().putString(KEY_TOKEN, token).apply()
    }
    fun getToken(ctx: Context): String? = prefs(ctx).getString(KEY_TOKEN, null)
    fun getBearerToken(ctx: Context): String {
        val raw = getToken(ctx).orEmpty().trim()
        if (raw.isEmpty()) return ""
        return if (raw.startsWith("Bearer ", true)) raw else "Bearer $raw"
    }

    // Role normalize: ADMIN | WAITER | UNKNOWN  (chấp nhận ROLE_ADMIN/ROLE_USER)
    private fun normalizeRole(raw: String?): String {
        val s0 = raw?.trim()?.uppercase().orEmpty()
        val s  = if (s0.startsWith("ROLE_")) s0.removePrefix("ROLE_") else s0
        return when (s) {
            "ADMIN" -> "ADMIN"
            "KITCHEN" -> "KITCHEN"
            "WAITER", "USER", "STAFF", "EMPLOYEE" -> "WAITER"
            else -> "UNKNOWN"
        }
    }
    fun saveRole(ctx: Context, rawRole: String?) {
        prefs(ctx).edit().putString(KEY_ROLE, normalizeRole(rawRole)).apply()
    }
    fun getRole(ctx: Context): String? = prefs(ctx).getString(KEY_ROLE, null)
    fun getRoleOrUnknown(ctx: Context): String = getRole(ctx) ?: "UNKNOWN"

    // Clear
    fun clear(ctx: Context) {
        prefs(ctx).edit().remove(KEY_TOKEN).remove(KEY_ROLE).apply()
    }
    fun clearAll(ctx: Context) {
        prefs(ctx).edit()
            .remove(KEY_TOKEN)
            .remove(KEY_ROLE)
            .remove(KEY_USER_JSON)
            .apply()
    }
    fun isLoggedIn(ctx: Context): Boolean = !getToken(ctx).isNullOrBlank()
}
