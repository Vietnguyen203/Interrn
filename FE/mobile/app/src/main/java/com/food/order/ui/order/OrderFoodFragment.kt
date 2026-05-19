package com.food.order.ui.order

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.food.order.adapter.FoodAdapter
import com.food.order.databinding.FragmentOrderFoodBinding
import com.food.order.ui.food.FoodViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.food.order.data.model.FoodModel
import com.food.order.databinding.DialogOrderOptionsBinding
import java.util.Collections

class OrderFoodFragment : Fragment() {

    private var _binding: FragmentOrderFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()
    private val userToken: String by lazy {
        ("Bearer " + requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("token", ""))
    }
    private val adapter: FoodAdapter by lazy {
        FoodAdapter(Collections.emptyList(), { item ->
            showOrderOptionsDialog(item)
        }, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderFoodBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.loadingFlow.collectLatest {
                    binding.loadingView.isVisible = it
                }
            }
            launch {
                viewModel.foodsFlow.collect { foods ->
                    binding.recyclerView.isVisible = foods.isNotEmpty()
                    binding.ivEmpty.isVisible = foods.isEmpty()
                    adapter.updateData(foods)
                }
            }
            launch {
                viewModel.addOrderItemFlow.collectLatest {
                    Toast.makeText(requireContext(), "Add order item success", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
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
            adapter = this@OrderFoodFragment.adapter
        }
        binding.cardViewBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.getFoodsFromServer(requireContext(), userToken)
    }

    private fun showOrderOptionsDialog(item: FoodModel) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogOrderOptionsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialogBinding.tvFoodName.text = item.foodName
        dialogBinding.tvFoodPrice.text = "${item.price} VND"

        var quantity = 1

        dialogBinding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                dialogBinding.tvQuantity.text = quantity.toString()
            }
        }

        dialogBinding.btnPlus.setOnClickListener {
            quantity++
            dialogBinding.tvQuantity.text = quantity.toString()
        }

        val checkBoxes = mutableListOf<android.widget.CheckBox>()
        item.options?.let { optStr ->
            val optionsList = optStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (optionsList.isEmpty()) {
                dialogBinding.tvOptionsLabel.visibility = View.GONE
                dialogBinding.llOptionsContainer.visibility = View.GONE
            } else {
                for (opt in optionsList) {
                    val cb = android.widget.CheckBox(requireContext()).apply {
                        text = opt
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    checkBoxes.add(cb)
                    dialogBinding.llOptionsContainer.addView(cb)
                }
            }
        } ?: run {
            dialogBinding.tvOptionsLabel.visibility = View.GONE
            dialogBinding.llOptionsContainer.visibility = View.GONE
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnAdd.setOnClickListener {
            val options = mutableListOf<String>()
            for (cb in checkBoxes) {
                if (cb.isChecked) options.add(cb.text.toString())
            }

            val manualNote = dialogBinding.etNote.text.toString().trim()

            val finalNoteBuilder = java.lang.StringBuilder()
            if (options.isNotEmpty()) {
                finalNoteBuilder.append("Tuỳ chọn: ").append(options.joinToString(", "))
            }
            if (manualNote.isNotEmpty()) {
                if (finalNoteBuilder.isNotEmpty()) finalNoteBuilder.append(" | ")
                finalNoteBuilder.append("Ghi chú: ").append(manualNote)
            }

            viewModel.addOrderItem(
                userToken, 
                requireArguments().getString("orderId") ?: "", 
                item, 
                quantity, 
                finalNoteBuilder.toString()
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
