package com.food.order.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.food.order.R
import com.food.order.data.SessionManager
import com.food.order.data.request.IngredientRequest
import com.food.order.data.request.StockTransactionRequest
import com.food.order.data.response.IngredientResponse
import com.food.order.databinding.FragmentInventoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InventoryViewModel by viewModels()

    private val userToken: String by lazy { SessionManager.getBearerToken(requireContext()) }

    private val ingredientAdapter: InventoryAdapter by lazy { 
        InventoryAdapter(emptyList()) { ingredient -> showIngredientActionDialog(ingredient) } 
    }
    private val transactionAdapter: TransactionAdapter by lazy { 
        TransactionAdapter(emptyList(), emptyList()) 
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)

        binding.rvIngredients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@InventoryFragment.ingredientAdapter
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@InventoryFragment.transactionAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.loadingFlow.collectLatest { loading ->
                    binding.loadingView.isVisible = loading
                }
            }
            launch {
                viewModel.ingredientsFlow.collectLatest { items ->
                    ingredientAdapter.updateData(items)
                    transactionAdapter.updateData(viewModel.transactionsFlow.value, items)
                    updateEmptyView()
                }
            }
            launch {
                viewModel.transactionsFlow.collectLatest { items ->
                    transactionAdapter.updateData(items, viewModel.ingredientsFlow.value)
                    updateEmptyView()
                }
            }
            launch {
                viewModel.errorFlow.collectLatest { msg ->
                    if (!msg.isNullOrBlank()) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewBack.setOnClickListener { findNavController().popBackStack() }
        binding.ivSync.setOnClickListener {
            loadData()
            Toast.makeText(requireContext(), "Đang đồng bộ...", Toast.LENGTH_SHORT).show()
        }

        binding.fabAdd.setOnClickListener { showAddIngredientDialog() }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isIngredientsTab = tab?.position == 0
                binding.rvIngredients.isVisible = isIngredientsTab
                binding.rvTransactions.isVisible = !isIngredientsTab
                binding.fabAdd.isVisible = isIngredientsTab
                updateEmptyView()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadData()
    }

    private fun loadData() {
        viewModel.getIngredients(userToken, requireContext())
        viewModel.getTransactions(userToken, requireContext())
    }

    private fun updateEmptyView() {
        val isIngredientsTab = binding.tabLayout.selectedTabPosition == 0
        if (isIngredientsTab) {
            val empty = viewModel.ingredientsFlow.value.isEmpty()
            binding.emptyView.isVisible = empty
            binding.rvIngredients.isVisible = !empty
        } else {
            val empty = viewModel.transactionsFlow.value.isEmpty()
            binding.emptyView.isVisible = empty
            binding.rvTransactions.isVisible = !empty
        }
    }

    private fun showIngredientActionDialog(ingredient: IngredientResponse) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_ingredient_action, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvIngredientName).text = ingredient.name

        view.findViewById<Button>(R.id.btnImport).setOnClickListener {
            dialog.dismiss()
            showStockTransactionDialog(ingredient, true)
        }

        view.findViewById<Button>(R.id.btnExport).setOnClickListener {
            dialog.dismiss()
            showStockTransactionDialog(ingredient, false)
        }

        view.findViewById<Button>(R.id.btnEdit).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Chức năng sửa thông tin sẽ sớm ra mắt", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showStockTransactionDialog(ingredient: IngredientResponse, isImport: Boolean) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_stock_transaction, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvTitle).text = if (isImport) "Nhập kho: ${ingredient.name}" else "Xuất kho: ${ingredient.name}"

        val etQty = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val etReason = view.findViewById<TextInputEditText>(R.id.etReason)

        view.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val qtyStr = etQty.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val reasonStr = etReason.text.toString().trim()

            if (qtyStr.isEmpty()) {
                etQty.error = "Vui lòng nhập số lượng"
                return@setOnClickListener
            }

            val qty = qtyStr.toDoubleOrNull() ?: 0.0
            val price = priceStr.toDoubleOrNull() ?: 0.0

            val request = StockTransactionRequest(ingredient.id, qty, price, reasonStr)

            if (isImport) {
                viewModel.importStock(userToken, requireContext(), request) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Nhập kho thành công", Toast.LENGTH_SHORT).show()
                }
            } else {
                viewModel.exportStock(userToken, requireContext(), request) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Xuất kho thành công", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showAddIngredientDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_ingredient, null)
        dialog.setContentView(view)

        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etUnit = view.findViewById<TextInputEditText>(R.id.etUnit)
        val etMinStock = view.findViewById<TextInputEditText>(R.id.etMinStock)

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            val unit = etUnit.text.toString().trim()
            val minStockStr = etMinStock.text.toString().trim()

            if (name.isEmpty() || unit.isEmpty() || minStockStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minStock = minStockStr.toDoubleOrNull() ?: 0.0
            val request = IngredientRequest(name, unit, minStock)

            viewModel.createIngredient(userToken, requireContext(), request) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Thêm nguyên liệu thành công", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
