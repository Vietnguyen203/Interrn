package com.food.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.food.order.R
import com.food.order.data.model.Order
import com.food.order.databinding.ItemOrderStatisticBinding

class OrderStatisticAdapter(
    private var data: List<Order>,
    private val onItemClick: (Order) -> Unit,
) : RecyclerView.Adapter<OrderStatisticAdapter.OrderStatisticViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Order>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class OrderStatisticViewHolder(
        val binding: ItemOrderStatisticBinding,
        val onItemClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            binding.tvWaiter.text = order.employeeName
            binding.tvOrder.text = order.id
            binding.tvTable.text = order.tableName
            binding.tvTotal.text = order.totalAmount.toString()
            binding.tvStatus.text = order.status
            val colorResId: Int = when (order.status) {
                "ORDERING" -> R.color.status_ordering
                "CANCELLED" -> R.color.status_cancelled
                else -> R.color.status_completed
            }

            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, colorResId))
            binding.root.setOnClickListener {
                onItemClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderStatisticViewHolder {
        val binding = ItemOrderStatisticBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderStatisticViewHolder(binding, onItemClick)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: OrderStatisticViewHolder, position: Int) {
        holder.bind(data[position])
    }
}