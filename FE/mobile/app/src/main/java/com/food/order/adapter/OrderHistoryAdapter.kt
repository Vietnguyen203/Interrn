package com.food.order.ui.order.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food.order.databinding.ItemOrderHistoryBinding
import com.food.order.data.remote.OrderRow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    private val onClick: (OrderRow) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.VH>() {

    private val data = mutableListOf<OrderRow>()
    private val moneyFmt = NumberFormat.getInstance(Locale("vi","VN"))
    private val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val out = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale("vi","VN"))

    fun submit(list: List<OrderRow>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemOrderHistoryBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(item: OrderRow) {
            b.tvTitle.text = "${item.tableName ?: "Bàn ?"} • ${item.status}"
            val whenStr = runCatching { out.format(iso.parse(item.createdAt)!!) }.getOrNull() ?: item.createdAt
            b.tvMeta.text = "Thu ngân: ${item.employeeName ?: "?"} • $whenStr"
            b.tvTotal.text = "Tổng: ${moneyFmt.format(item.totalAmount)} ₫"
            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOrderHistoryBinding.inflate(inflater, parent, false)
        return VH(binding)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(data[position])
    override fun getItemCount(): Int = data.size
}
