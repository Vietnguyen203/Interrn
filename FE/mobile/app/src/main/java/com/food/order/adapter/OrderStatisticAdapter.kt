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
            
            // Format totalAmount beautifully in Vietnamese Dong
            val moneyFmt = java.text.NumberFormat.getInstance(java.util.Locale("vi", "VN"))
            val formattedTotal = order.totalAmount?.let { moneyFmt.format(it) + " ₫" } ?: "0 ₫"
            binding.tvTotal.text = formattedTotal
            
            binding.tvStatus.text = order.status
            val colorResId: Int = when (order.status) {
                "ORDERING" -> R.color.status_ordering
                "CANCELLED" -> R.color.status_cancelled
                else -> R.color.status_completed
            }

            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, colorResId))

            // Populate items dynamically
            binding.layoutItems.removeAllViews()
            if (order.items.isNotEmpty()) {
                binding.dividerItems.visibility = android.view.View.VISIBLE
                binding.layoutItemsHeader.visibility = android.view.View.VISIBLE
                
                val context = binding.root.context
                val inflater = LayoutInflater.from(context)
                
                for (item in order.items) {
                    val dishView = inflater.inflate(R.layout.item_order_detail_dish, binding.layoutItems, false)
                    val tvDishName = dishView.findViewById<android.widget.TextView>(R.id.tvDishName)
                    val tvDishQty = dishView.findViewById<android.widget.TextView>(R.id.tvDishQty)
                    val tvDishPrice = dishView.findViewById<android.widget.TextView>(R.id.tvDishPrice)
                    
                    tvDishName.text = item.foodName ?: "Món ăn ẩn"
                    tvDishQty.text = "x${item.quantity ?: 0}"
                    
                    val itemTotal = (item.price ?: 0.0) * (item.quantity ?: 0)
                    tvDishPrice.text = moneyFmt.format(itemTotal) + " ₫"
                    
                    binding.layoutItems.addView(dishView)
                }
            } else {
                binding.dividerItems.visibility = android.view.View.GONE
                binding.layoutItemsHeader.visibility = android.view.View.GONE
            }

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