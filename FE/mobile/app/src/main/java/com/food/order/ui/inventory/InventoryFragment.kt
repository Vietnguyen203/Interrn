package com.food.order.ui.inventory

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
import com.food.order.data.SessionManager
import com.food.order.databinding.FragmentInventoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InventoryViewModel by viewModels()

    private val userToken: String by lazy { SessionManager.getBearerToken(requireContext()) }

    private val adapter: InventoryAdapter by lazy { InventoryAdapter(emptyList()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@InventoryFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.loadingFlow.collectLatest { loading ->
                    binding.loadingView.isVisible = loading
                }
            }
            launch {
                viewModel.ingredientsFlow.collectLatest { items ->
                    binding.recyclerView.isVisible = items.isNotEmpty()
                    binding.emptyView.isVisible = items.isEmpty()
                    adapter.updateData(items)
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
            viewModel.getIngredients(userToken, requireContext())
            Toast.makeText(requireContext(), "Đang đồng bộ...", Toast.LENGTH_SHORT).show()
        }

        viewModel.getIngredients(userToken, requireContext())
    }

    override fun onResume() {
        super.onResume()
        viewModel.getIngredients(userToken, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
