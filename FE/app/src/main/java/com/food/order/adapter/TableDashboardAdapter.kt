package com.food.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food.order.R
import com.food.order.data.model.TableModel
import com.food.order.databinding.ItemTableViewBinding

class TableDashboardAdapter(
    private var data: List<TableModel>,
    private val onItemClick: (TableModel) -> Unit
) : RecyclerView.Adapter<TableDashboardAdapter.TableViewHolder>() {

    init { setHasStableIds(true) }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<TableModel>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class TableViewHolder(
        val binding: ItemTableViewBinding,
        val onItemClick: (TableModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(table: TableModel) {
            // ✅ Dùng đúng field đang có trong TableModel
            binding.tvName.text = table.name

            binding.root.setOnClickListener { onItemClick(table) }

            if (table.currentOrderId != null) {
                binding.tv.text = "Using"
                binding.tv.setBackgroundResource(R.drawable.bg_occupied_table_view)
            } else {
                binding.tv.text = "Free"
                binding.tv.setBackgroundResource(R.drawable.bg_empty_table_view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemTableViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TableViewHolder(binding, onItemClick)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemId(position: Int): Long = data[position].id.hashCode().toLong()
}
