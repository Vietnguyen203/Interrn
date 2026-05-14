package com.food.order.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {
    private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatBirthday(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"
        if (dateString.contains("/")) return dateString // Already in display format
        return try {
            val date = apiFormat.parse(dateString)
            if (date != null) displayFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun toApiDate(displayDate: String?): String? {
        if (displayDate.isNullOrEmpty() || displayDate == "N/A") return null
        if (displayDate.contains("-")) return displayDate // Already in API format
        return try {
            val date = displayFormat.parse(displayDate)
            if (date != null) apiFormat.format(date) else null
        } catch (e: Exception) {
            null
        }
    }
}
