package com.food.order.ui.kitchen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.food.order.data.SessionManager
import com.food.order.databinding.FragmentKitchenBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KitchenFragment : Fragment() {

    private var _binding: FragmentKitchenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KitchenViewModel by viewModels()
    private val userToken: String by lazy { SessionManager.getBearerToken(requireContext()) }

    private val kitchenAdapter: KitchenAdapter by lazy {
        KitchenAdapter(emptyList()) { item, targetStatus ->
            viewModel.updateItemStatus(userToken, item.orderItemId, targetStatus)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKitchenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvKitchenItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = kitchenAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchKitchenItems(userToken)
        }

        registerObservers()
        viewModel.fetchKitchenItems(userToken)
    }

    private fun registerObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.itemsFlow.collectLatest { items ->
                    kitchenAdapter.updateData(items)
                    binding.tvEmpty.isVisible = items.isEmpty()
                }
            }
            launch {
                viewModel.loadingFlow.collectLatest { isLoading ->
                    binding.swipeRefresh.isRefreshing = isLoading
                }
            }
            launch {
                viewModel.errorFlow.collectLatest { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
