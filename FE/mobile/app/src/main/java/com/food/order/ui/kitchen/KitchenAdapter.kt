package com.food.order.ui.kitchen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food.order.data.response.OrderItemResponse
import com.food.order.databinding.ItemKitchenOrderBinding

class KitchenAdapter(
    private var items: List<OrderItemResponse>,
    private val onActionClick: (OrderItemResponse, String) -> Unit
) : RecyclerView.Adapter<KitchenAdapter.VH>() {

    inner class VH(val b: ItemKitchenOrderBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemKitchenOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.apply {
            tvFoodName.text = item.foodName ?: "Unknown Item"
            tvQuantity.text = "Quantity: ${item.quantity}"
            tvStatus.text = item.status
            
            if (!item.note.isNullOrBlank()) {
                tvNote.visibility = View.VISIBLE
                tvNote.text = "Note: ${item.note}"
            } else {
                tvNote.visibility = View.GONE
            }

            when (item.status) {
                "PENDING" -> {
                    btnAction.text = "Accept Order"
                    btnAction.visibility = View.VISIBLE
                    btnAction.setOnClickListener { onActionClick(item, "PREPARING") }
                }
                "PREPARING" -> {
                    btnAction.text = "Mark Completed"
                    btnAction.visibility = View.VISIBLE
                    btnAction.setOnClickListener { onActionClick(item, "READY") }
                }
                else -> {
                    btnAction.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<OrderItemResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}
