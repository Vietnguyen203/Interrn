package com.food.order.ui.order

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.food.order.R
import com.food.order.adapter.OrderFoodAdapter
import com.food.order.data.model.OrderItem
import com.food.order.databinding.FragmentOrderTableBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Collections

class OrderTableFragment : Fragment() {
    private var _binding: FragmentOrderTableBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderTableViewModel by viewModels()

    private val userToken: String by lazy {
        ("Bearer " + requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("token", ""))
    }

    private val adapter: OrderFoodAdapter by lazy {
        OrderFoodAdapter(Collections.emptyList()) { item ->
            AlertDialog.Builder(requireContext())
                .setTitle("Notification")
                .setMessage("Do you want to remove this item from your bill?")
                .setPositiveButton("OK") { dialog, _ ->
                    val foodId = extractFoodId(item)
                    if (foodId.isNullOrBlank()) {
                        Toast.makeText(requireContext(), "Cannot find foodId for this item", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.removeItemFromOrder(userToken, foodId)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderTableBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.tableFlow.collectLatest {
                    binding.tvTableName.text = it?.name
                }
            }
            launch {
                viewModel.employeeOrderFlow.collectLatest {
                    binding.tvWaiter.text = it?.createdBy
                }
            }
            launch {
                viewModel.copyOrderFlow.collectLatest {
                    Toast.makeText(requireContext(), "Copy order successfully", Toast.LENGTH_SHORT).show()
                    viewModel.getDetailTable(userToken)
                }
            }
            launch {
                viewModel.orderFlow.collectLatest { order ->
                    val items = order.data.items ?: emptyList()
                    adapter.updateData(items)

                    binding.tvCreateAt.text = order.data.createdAt

                    val serverTotal = order.data.totalAmount ?: 0.0
                    val computedTotal = items.sumOf { (it.price ?: 0.0) * ((it.quantity ?: 0).toDouble()) }
                    val total = if (serverTotal > 0.0) serverTotal else computedTotal

                    binding.tvTotalAmount.text = formatVnd(total)
                }
            }
            launch {
                viewModel.cancelOrderFlow.collectLatest {
                    findNavController().popBackStack()
                }
            }
            launch {
                viewModel.completedOrderFlow.collectLatest {
                    Toast.makeText(requireContext(), "Completed order successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            launch {
                viewModel.removeItemFromOrderFlow.collectLatest {
                    Toast.makeText(requireContext(), "Remove item from order successfully", Toast.LENGTH_SHORT).show()
                    viewModel.getDetailTable(userToken)
                }
            }
            launch {
                viewModel.tablesFreeFlow.collectLatest {
                    if (it.isEmpty()) {
                        Toast.makeText(requireContext(), "There are no free tables", Toast.LENGTH_SHORT).show()
                    } else {
                        val tableNames = Array(it.size) { idx -> it[idx].name }
                        AlertDialog.Builder(context)
                            .setTitle("Choose a free table")
                            .setItems(tableNames) { dialog, which ->
                                dialog.dismiss()
                                viewModel.copyTableOrder(userToken, it[which].id)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@OrderTableFragment.adapter
        }

        binding.cardViewBack.setOnClickListener { findNavController().popBackStack() }

        binding.cardCopyOrder.setOnClickListener { viewModel.getTablesFromServer(userToken) }

        binding.btnCompleted.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Notification")
                .setMessage("Confirm payment of this invoice?")
                .setPositiveButton("OK") { d, _ ->
                    d.dismiss()
                    viewModel.completeOrder(userToken)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.cardOrder.setOnClickListener {
            val bundle = Bundle().apply { putString("orderId", viewModel.orderId) }
            findNavController().navigate(
                R.id.action_navigation_order_table_to_navigation_order_food, bundle
            )
        }

        binding.btnCancel.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Notification")
                .setMessage("Are you sure you want to cancel your order?")
                .setPositiveButton("OK") { dialog, _ ->
                    viewModel.cancelOrder(userToken)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewModel.apply {
            setArguments(arguments)
            getDetailTable(userToken)
            getCreateByOrder(userToken)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatVnd(value: Double): String {
        return DecimalFormat("#,###").format(value) + " VNĐ"
    }

    /**
     * Lấy foodId từ OrderItem mà **không** cần biết tên field thật sự là gì.
     * Thử lần lượt: item.foodId, item.id, item.food.id, item.food.foodId
     */
    private fun extractFoodId(item: OrderItem): String? {
        fun field(obj: Any?, name: String): Any? {
            if (obj == null) return null
            return try {
                val f = obj.javaClass.declaredFields.firstOrNull { it.name == name } ?: return null
                f.isAccessible = true
                f.get(obj)
            } catch (_: Exception) { null }
        }

        val directFoodId = field(item, "foodId")?.toString()
        if (!directFoodId.isNullOrBlank()) return directFoodId

        val directId = field(item, "id")?.toString()
        if (!directId.isNullOrBlank()) return directId

        val foodObj = field(item, "food")
        val nestedId = field(foodObj, "id")?.toString()
        if (!nestedId.isNullOrBlank()) return nestedId

        val nestedFoodId = field(foodObj, "foodId")?.toString()
        if (!nestedFoodId.isNullOrBlank()) return nestedFoodId

        return null
    }
}
