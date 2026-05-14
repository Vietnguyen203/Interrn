package com.food.order.utils

import android.content.Context
import com.food.order.data.RetrofitClient
import com.food.order.data.UrlUtils
import com.food.order.ultis.SlugUtils

object ImageResolver {

    /**
     * Trả về nguồn ảnh cho Glide (có thể là Int resource id, asset URL, hay http URL).
     * Ưu tiên: drawable (res) -> assets/foods -> đường dẫn từ server (field image).
     */
    @JvmStatic
    fun forFood(context: Context, foodName: String?, imagePath: String?): Any? {
        localByName(context, foodName)?.let { return it }
        val remote = UrlUtils.joinFileUrl(RetrofitClient.FILE_BASE_URL, imagePath)
        return if (remote.isNotBlank()) remote else null
    }

    /** Tìm trong drawable và assets theo tên món (slug) */
    @JvmStatic
    fun localByName(context: Context, foodName: String?): Any? {
        if (foodName.isNullOrBlank()) return null
        val slug = SlugUtils.toSlug(foodName)

        // 1) drawable resource: tên dùng underscore (Android không dùng '-')
        val drawableName = slug.replace("-", "_")
        val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        if (resId != 0) return resId

        // 2) assets: app/src/main/assets/foods/<slug>.<ext>
        val exts = arrayOf("jpg", "jpeg", "png", "webp", "avif", "gif")
        for (ext in exts) {
            val assetPath = "foods/$slug.$ext"
            if (assetExists(context, assetPath)) {
                return "file:///android_asset/$assetPath"
            }
        }
        return null
    }

    private fun assetExists(context: Context, path: String): Boolean {
        return try {
            context.assets.open(path).close(); true
        } catch (_: Exception) {
            false
        }
    }
}
