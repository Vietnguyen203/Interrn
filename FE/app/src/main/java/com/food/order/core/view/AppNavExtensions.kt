package com.food.order.core.view

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.NavDirections
import androidx.navigation.navOptions

/**
 * Điều hướng an toàn:
 * - Hỗ trợ cả actionId lẫn destinationId.
 * - Không crash nếu state đã save / id không hợp lệ với destination hiện tại.
 * - Mặc định launchSingleTop + restoreState để tránh đúp màn hình.
 */
fun NavController.safeNavigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    options: NavOptions? = defaultNavOptions()
) {
    try {
        // 1) Thử coi như action trước (từ currentDestination hoặc từ graph gốc)
        val action = currentDestination?.getAction(resId) ?: (graph as NavGraph).getAction(resId)
        if (action != null) {
            navigate(resId, args, options)
            return
        }
        // 2) Không phải action → thử coi như destination id có trong graph
        if (graph.findNode(resId) != null) {
            navigate(resId, args, options)
            return
        }
        // 3) Không tìm thấy: im lặng bỏ qua để tránh crash khi id sai
    } catch (_: IllegalArgumentException) {
        // destination/action không hợp lệ với state hiện tại → bỏ qua
    } catch (_: IllegalStateException) {
        // state đã save (đổi orientation/đưa app nền) → bỏ qua
    }
}

/** Overload cho NavDirections (Safe Args). */
fun NavController.safeNavigate(
    directions: NavDirections,
    options: NavOptions? = defaultNavOptions()
) {
    try {
        navigate(directions, options)
    } catch (_: IllegalArgumentException) {
        // destination/action không hợp lệ với state hiện tại
    } catch (_: IllegalStateException) {
        // state đã save
    }
}

private fun defaultNavOptions(): NavOptions = navOptions {
    launchSingleTop = true
    restoreState = true
    // Không popUpTo mặc định để giữ back stack trừ khi bạn muốn hành vi khác
}
