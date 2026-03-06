package com.food.order.ui.staff

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
import com.food.order.R
import com.food.order.adapter.StaffAdapter
import com.food.order.data.SessionManager
import com.food.order.databinding.FragmentStaffBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StaffFragment : Fragment() {
    private var _binding: FragmentStaffBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffViewModel by viewModels()
    private val adapter: StaffAdapter by lazy {
        StaffAdapter(emptyList()) { user ->
            val bundle = Bundle().apply { putSerializable("edit_staff", user) }
            findNavController().navigate(
                R.id.action_navigation_staff_to_navigation_update_staff,
                bundle
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStaffBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.loadingFlow.collectLatest { loading ->
                    if (loading) binding.loadingView.show() else binding.loadingView.hide()
                }
            }
            launch {
                viewModel.errorFlow.collectLatest { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
            launch {
                viewModel.usersFlow.collectLatest { users ->
                    adapter.updateData(users.map { it.toUserModel() })
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@StaffFragment.adapter
        }
        binding.cardViewBack.setOnClickListener { findNavController().popBackStack() }
        binding.tvAddStaff.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_staff_to_navigation_create_staff)
        }

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val bearerToken = SessionManager.getBearerToken(requireContext())
        val serverAddress = prefs.getString("server_address", "") ?: ""
        viewModel.getUserFromServer(bearerToken, serverAddress)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
