package com.food.order.ui.inventory

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.food.order.R
import com.food.order.data.response.IngredientResponse

class InventoryAdapter(
    private var ingredients: List<IngredientResponse>,
    private val onItemClick: (IngredientResponse) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newIngredients: List<IngredientResponse>) {
        this.ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = ingredients[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = ingredients.size

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        private val tvUnit: TextView = itemView.findViewById(R.id.tvUnit)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)

        @SuppressLint("SetTextI18n")
        fun bind(item: IngredientResponse) {
            tvName.text = item.name
            
            val stockStr = if (item.currentStock % 1.0 == 0.0) {
                item.currentStock.toInt().toString()
            } else {
                item.currentStock.toString()
            }
            tvStock.text = "$stockStr ${item.unit}"
            tvUnit.visibility = View.GONE

            val isLowStock = item.currentStock <= item.minStockLevel
            if (isLowStock) {
                tvStatus.text = "Sắp hết (${item.minStockLevel} ${item.unit})"
                tvStatus.setTextColor(Color.parseColor("#EF4444")) // Red
                tvStock.setTextColor(Color.parseColor("#EF4444"))
                ivIcon.setColorFilter(Color.parseColor("#EF4444"))
            } else {
                tvStatus.text = "Tồn kho an toàn"
                tvStatus.setTextColor(Color.parseColor("#10B981")) // Green
                tvStock.setTextColor(Color.parseColor("#11117F")) // Primary
                ivIcon.setColorFilter(Color.parseColor("#11117F"))
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
