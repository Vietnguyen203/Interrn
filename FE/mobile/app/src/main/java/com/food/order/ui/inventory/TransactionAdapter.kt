package com.food.order.ui.inventory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.food.order.R
import com.food.order.data.response.IngredientResponse
import com.food.order.data.response.TransactionResponse
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<TransactionResponse>,
    private var ingredients: List<IngredientResponse>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    fun updateData(newTransactions: List<TransactionResponse>, newIngredients: List<IngredientResponse>) {
        transactions = newTransactions
        ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = transactions[position]
        val ingredient = ingredients.find { it.id == tx.ingredientId }
        holder.bind(tx, ingredient)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvIngredientName: TextView = itemView.findViewById(R.id.tvIngredientName)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvReason: TextView = itemView.findViewById(R.id.tvReason)

        fun bind(tx: TransactionResponse, ingredient: IngredientResponse?) {
            tvIngredientName.text = ingredient?.name ?: "Nguyên liệu ẩn"
            
            // Format Date
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = parser.parse(tx.createdAt)
                tvDate.text = if (date != null) formatter.format(date) else tx.createdAt
            } catch (e: Exception) {
                tvDate.text = tx.createdAt
            }

            val isImport = tx.transactionType == "IMPORT"
            val isSale = tx.transactionType == "EXPORT_SALE"

            if (isImport) {
                tvType.text = "Nhập kho"
                tvType.setTextColor(Color.parseColor("#10B981"))
                tvQuantity.text = "+${tx.quantity}"
                tvQuantity.setTextColor(Color.parseColor("#10B981"))
            } else {
                tvType.text = if (isSale) "Xuất bán" else "Xuất hủy/khác"
                tvType.setTextColor(Color.parseColor(if (isSale) "#6366F1" else "#EF4444"))
                tvQuantity.text = "-${tx.quantity}"
                tvQuantity.setTextColor(Color.parseColor(if (isSale) "#6366F1" else "#EF4444"))
            }

            if (!tx.reason.isNullOrBlank()) {
                tvReason.visibility = View.VISIBLE
                tvReason.text = tx.reason
            } else {
                tvReason.visibility = View.GONE
            }
        }
    }
}
