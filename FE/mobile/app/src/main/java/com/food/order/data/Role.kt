package com.food.order.data

enum class Role { ADMIN, WAITER, KITCHEN, UNKNOWN;
    companion object {
        fun from(raw: String?): Role {
            val s0 = raw?.trim()?.uppercase() ?: return UNKNOWN
            val s  = if (s0.startsWith("ROLE_")) s0.removePrefix("ROLE_") else s0
            return when (s) {
                "ADMIN" -> ADMIN
                "KITCHEN" -> KITCHEN
                "WAITER", "USER", "STAFF", "EMPLOYEE" -> WAITER
                else -> UNKNOWN
            }
        }
    }
}
