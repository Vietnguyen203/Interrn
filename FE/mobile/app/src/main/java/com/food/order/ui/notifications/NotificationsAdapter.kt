package com.food.order.ui.notifications

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food.order.data.response.NotificationResponse
import com.food.order.databinding.ItemNotificationBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationsAdapter(
    private var items: List<NotificationResponse>,
    private val onItemClick: (NotificationResponse) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.VH>() {

    inner class VH(val b: ItemNotificationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.apply {
            tvTitle.text = item.title ?: "Thông báo"
            tvMessage.text = item.message ?: ""
            tvTime.text = formatTime(item.createdAt)

            // Type color
            val dotColor = when (item.type) {
                "success" -> "#10B981"
                "error"   -> "#EF4444"
                "warning" -> "#F59E0B"
                else      -> "#3B82F6"  // info
            }
            try { viewTypeDot.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor(dotColor))
            } catch (_: Exception) {}

            // Background: đưa chưa đọc
            itemRoot.setBackgroundColor(
                if (!item.read) Color.parseColor("#EFF6FF") else Color.WHITE
            )
            tvTitle.setTextColor(
                if (!item.read) Color.parseColor("#1D4ED8") else Color.parseColor("#1E293B")
            )

            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<NotificationResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatTime(createdAt: String?): String {
        if (createdAt == null) return ""
        return try {
            val dt = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val now = LocalDateTime.now()
            val minutes = ChronoUnit.MINUTES.between(dt, now)
            when {
                minutes < 1  -> "Vừa xong"
                minutes < 60 -> "${minutes} phút trước"
                minutes < 1440 -> "${minutes / 60} giờ trước"
                else -> "${minutes / 1440} ngày trước"
            }
        } catch (_: Exception) { "" }
    }
}
