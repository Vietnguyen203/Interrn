package com.food.order.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food.order.databinding.ItemDashboardMenuBinding

class MenuAdapter(
    private val items: List<MenuItemModel>,
    private val onClick: (MenuItemModel) -> Unit
) : RecyclerView.Adapter<MenuAdapter.VH>() {

    inner class VH(val b: ItemDashboardMenuBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemDashboardMenuBinding.inflate(inf, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvTitle.text = item.title
        if (item.iconRes != 0) {
            holder.b.ivIcon.setImageResource(item.iconRes)
            holder.b.ivIcon.visibility = View.VISIBLE
        } else {
            holder.b.ivIcon.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
