package com.food.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food.order.data.model.TableModel
import com.food.order.databinding.ItemTableBinding

class TableAdapter(
    private var data: List<TableModel>,
    private val onItemClick: (TableModel) -> Unit
) : RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<TableModel>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class TableViewHolder(
        val binding: ItemTableBinding,
        val onItemClick: (TableModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(table: TableModel) {
            binding.tvTableName.text = table.name
            binding.root.setOnClickListener {
                onItemClick(table)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableViewHolder(binding, onItemClick)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(data[position])
    }
}