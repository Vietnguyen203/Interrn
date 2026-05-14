package com.food.order.data.request

/**
 * Body dùng cho PUT /users/{server}/{employeeId}
 * Tuỳ nhu cầu, bạn gửi field nào thì BE cập nhật field đó.
 * Lưu ý: FE đã cấu hình Gson LOWER_CASE_WITH_UNDERSCORES -> fullName => full_name
 */
data class UpdateStaffRequest(
    val fullName: String? = null,
    val role: String? = null,
    val password: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,       // "MALE" | "FEMALE" | "OTHER"
    val birthday: String? = null      // "yyyy-MM-dd" nếu dùng
)
