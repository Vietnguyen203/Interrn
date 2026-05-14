package com.food.order.ui.auth

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.food.order.R
import com.food.order.data.SessionManager

class LogoutDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Logout") { _, _ ->
                // Xoá toàn bộ phiên: token + role + các cache liên quan
                SessionManager.clearAll(requireContext())

                // Điều hướng về Login và xoá toàn bộ back stack
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true)
                    .build()
                findNavController().navigate(R.id.navigation_login, null, navOptions)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
