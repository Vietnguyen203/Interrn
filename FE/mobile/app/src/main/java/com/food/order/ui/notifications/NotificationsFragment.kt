package com.food.order.ui.notifications

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
import com.food.order.databinding.FragmentNotificationsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()
    private val userToken: String by lazy { SessionManager.getBearerToken(requireContext()) }
    private val userRole: String by lazy { SessionManager.getRole(requireContext()) ?: "KITCHEN" }

    private val adapter: NotificationsAdapter by lazy {
        NotificationsAdapter(
            items = emptyList(),
            onItemClick = { notification ->
                if (!notification.read && notification.id != null) {
                    viewModel.markAsRead(userToken, notification.id)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationsFragment.adapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchNotifications(userToken, userRole)
        }

        observeViewModel()
        viewModel.fetchNotifications(userToken, userRole)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.notificationsFlow.collectLatest { list ->
                    adapter.updateData(list)
                    binding.tvEmpty.isVisible = list.isEmpty()
                    binding.rvNotifications.isVisible = list.isNotEmpty()

                    val unread = list.count { !it.read }
                    binding.tvUnreadBadge.isVisible = unread > 0
                    binding.tvUnreadBadge.text = "$unread chưa đọc"
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