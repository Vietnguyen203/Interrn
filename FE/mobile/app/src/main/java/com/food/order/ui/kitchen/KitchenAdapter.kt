package com.food.order.ui.kitchen

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food.order.databinding.ItemKitchenOrderBinding
import com.food.order.databinding.ItemKitchenSubItemBinding

class KitchenAdapter(
    private var groups: List<KitchenTableGroup>,
    private val onActionClick: (KitchenItem, String) -> Unit,
    private val onCompleteAllClick: (List<KitchenItem>) -> Unit
) : RecyclerView.Adapter<KitchenAdapter.VH>() {

    inner class VH(val b: ItemKitchenOrderBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemKitchenOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val group = groups[position]
        holder.b.apply {
            tvTableNumber.text = group.tableNumber
            tvItemCount.text = "${group.items.size} món"

            // Check if there are active cooking or pending items
            val hasActiveItems = group.items.any { it.kitchenStatus == "PENDING" || it.kitchenStatus == "COOKING" }
            if (hasActiveItems) {
                btnCompleteAll.visibility = View.VISIBLE
                btnCompleteAll.setOnClickListener { onCompleteAllClick(group.items) }
            } else {
                btnCompleteAll.visibility = View.GONE
            }

            // Populate child items dynamically
            containerItems.removeAllViews()
            val inflater = LayoutInflater.from(holder.itemView.context)
            group.items.forEach { item ->
                val itemBinding = ItemKitchenSubItemBinding.inflate(inflater, containerItems, false)
                itemBinding.tvFoodName.text = item.foodName
                itemBinding.tvQuantity.text = "x${item.quantity}"

                val ks = item.kitchenStatus
                val statusText = when (ks) {
                    "PENDING" -> "Chờ"
                    "COOKING" -> "Đang nấu"
                    "READY" -> "Xong"
                    else -> ks
                }
                itemBinding.tvStatus.text = statusText

                val statusColor = when (ks) {
                    "PENDING" -> "#D97706" // orange
                    "COOKING" -> "#EF4444" // red
                    "READY" -> "#10B981" // green
                    else -> "#475569"
                }
                val statusBg = when (ks) {
                    "PENDING" -> "#FEF3C7"
                    "COOKING" -> "#FEE2E2"
                    "READY" -> "#D1FAE5"
                    else -> "#F1F5F9"
                }
                try {
                    itemBinding.tvStatus.setTextColor(Color.parseColor(statusColor))
                    itemBinding.tvStatus.setBackgroundColor(Color.parseColor(statusBg))
                } catch (_: Exception) {}

                if (!item.note.isNullOrBlank()) {
                    itemBinding.tvNote.visibility = View.VISIBLE
                    itemBinding.tvNote.text = "Ghi chú: ${item.note}"
                } else {
                    itemBinding.tvNote.visibility = View.GONE
                }

                when (ks) {
                    "PENDING" -> {
                        itemBinding.btnAction.text = "Nấu"
                        itemBinding.btnAction.visibility = View.VISIBLE
                        itemBinding.btnAction.setOnClickListener { onActionClick(item, "COOKING") }
                    }
                    "COOKING" -> {
                        itemBinding.btnAction.text = "Xong"
                        itemBinding.btnAction.visibility = View.VISIBLE
                        itemBinding.btnAction.setOnClickListener { onActionClick(item, "READY") }
                    }
                    else -> {
                        itemBinding.btnAction.visibility = View.GONE
                    }
                }

                containerItems.addView(itemBinding.root)
            }
        }
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<KitchenTableGroup>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
