package com.food.order.ui.food

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
import com.food.order.R
import com.food.order.adapter.FoodAdapter
import com.food.order.data.SessionManager
import com.food.order.data.model.FoodModel
import com.food.order.databinding.FragmentFoodBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Collections

class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()

    // ✅ CHANGED: token chuẩn “Bearer …”
    private val userToken: String by lazy { SessionManager.getBearerToken(requireContext()) }

    private val adapter: FoodAdapter by lazy {
        FoodAdapter(
            Collections.emptyList(),
            { item ->
                val bundle = Bundle().apply { putSerializable("edit_food", item) }
                findNavController().navigate(
                    R.id.action_navigation_food_to_navigation_update_food,
                    bundle
                )
            },
            false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)

        // Gắn adapter trước
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@FoodFragment.adapter
            visibility = View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // loading
            launch {
                viewModel.loadingFlow.collectLatest { loading ->
                    binding.loadingView.isVisible = loading
                }
            }
            // data — ViewModel emit FoodModel trực tiếp
            launch {
                viewModel.foodsFlow.collectLatest { items ->
                    binding.recyclerView.isVisible = items.isNotEmpty()
                    binding.ivEmpty.isVisible = items.isEmpty()
                    runCatching { adapter.updateData(items) }
                        .onFailure {
                            Toast.makeText(requireContext(), "Lỗi hiển thị: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            // error
            launch {
                viewModel.errorFlow.collectLatest { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewBack.setOnClickListener { findNavController().popBackStack() }
        binding.tvCreateFood.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_food_to_navigation_create_food)
        }

        // gọi lần đầu
        viewModel.getFoodsFromServer(requireContext(), userToken)
    }

    override fun onResume() {
        super.onResume()
        // refresh mỗi lần quay lại
        viewModel.getFoodsFromServer(requireContext(), userToken)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
