package com.food.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.food.order.R
import com.food.order.data.model.OrderItem
import com.food.order.databinding.ItemOrderFoodBinding
import com.food.order.utils.ImageResolver

class OrderFoodAdapter(
    private var data: List<OrderItem>,
    private val onItemClick: (OrderItem) -> Unit,        // long-press callback
) : RecyclerView.Adapter<OrderFoodAdapter.OrderFoodViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<OrderItem>) {
        data = newData ?: emptyList()
        notifyDataSetChanged()
    }

    // Hỗ trợ swipe-to-delete (nếu dùng)
    fun getItemAt(position: Int): OrderItem? =
        if (position in 0 until itemCount) data[position] else null

    inner class OrderFoodViewHolder(
        val binding: ItemOrderFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: OrderItem) {
            binding.tvFoodName.text = item.foodName ?: ""
            binding.tvAmount.text = "${item.price}VNĐ"
            binding.tvQuantity.text = "${item.quantity}"

            val source = ImageResolver.forFood(binding.root.context, item.foodName, item.foodImage)
            Glide.with(binding.root)
                .load(source)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.ivFood)

            // Long-press => callback xoá
            binding.root.setOnLongClickListener {
                onItemClick(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderFoodViewHolder {
        val binding = ItemOrderFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderFoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderFoodViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
