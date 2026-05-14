package com.food.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.food.order.R
import com.food.order.data.model.FoodModel
import com.food.order.databinding.ItemFoodBinding
import com.food.order.utils.ImageResolver

class FoodAdapter @JvmOverloads constructor(
    private var data: List<FoodModel>,
    private val onItemClick: (FoodModel) -> Unit,
    private val isOrder: Boolean = false,
    private val onItemLongClick: ((FoodModel) -> Unit)? = null, // ✅ thêm long-press
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<FoodModel>) {
        data = newData
        notifyDataSetChanged()
    }

    // ✅ hỗ trợ lấy item theo vị trí (nếu cần swipe)
    fun getItemAt(position: Int): FoodModel? =
        if (position in 0 until data.size) data[position] else null

    inner class FoodViewHolder(
        val binding: ItemFoodBinding,
        val onItemClick: (FoodModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(food: FoodModel) {
            binding.tvName.text = food.foodName
            binding.tvPrice.text = "${food.price} VNĐ / ${food.unit}"
            binding.tvCreateAt.text = food.createdAt
            binding.tvCreateBy.text = food.createdBy
            binding.tvCategory.text = food.category

            if (food.description.isNullOrEmpty()) {
                binding.layoutDescription.isVisible = false
            } else {
                binding.layoutDescription.isVisible = true
                binding.tvDescription.text = food.description
            }

            binding.layoutCreateAt.isVisible = !isOrder
            binding.layoutCreateBy.isVisible = !isOrder

            val source = ImageResolver.forFood(binding.root.context, food.foodName, food.image)

            Glide.with(binding.root)
                .load(source)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.ivFood)

            // Tap = thêm vào order (giữ nguyên behavior cũ)
            binding.root.setOnClickListener { onItemClick(food) }

            // Long-press = xoá khỏi order (nếu truyền callback)
            binding.root.setOnLongClickListener {
                onItemLongClick?.invoke(food)
                onItemLongClick != null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding, onItemClick)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(data[position])
    }
}
