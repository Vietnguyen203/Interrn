package com.food.order.data

object UrlUtils {
    /**
     * Ghép URL ảnh an toàn.
     * - Nếu path đã là http(s) → trả về nguyên.
     * - Nếu path rỗng/null → trả về "" (Glide sẽ dùng placeholder).
     * - Nếu path là tương đối → ghép vào base (đảm bảo chỉ 1 dấu /).
     */
    @JvmStatic
    fun joinFileUrl(baseNoSlash: String, pathMaybeNull: String?): String {
        val p = (pathMaybeNull ?: "").trim()
        if (p.isEmpty()) return ""
        if (p.startsWith("http://", true) || p.startsWith("https://", true)) return p

        val base = baseNoSlash.trimEnd('/')
        val path = if (p.startsWith("/")) p else "/$p"
        return base + path
    }
}
