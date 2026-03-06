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
import com.food.order.data.mapper.toFoodModel
import com.food.order.databinding.FragmentOrderFoodBinding
import com.food.order.ui.food.FoodViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
            viewModel.addOrderItem(userToken, requireArguments().getString("orderId") ?: "", item)
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
                    adapter.updateData(foods.map { it.toFoodModel() })
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

        viewModel.getFoodsFromServer(userToken)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}