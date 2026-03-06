package com.food.order.ui.splash

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.food.order.R
import com.food.order.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedServer = sharedPref.getString("server_address", "")
        binding.tvServer.text = "Server: $savedServer\nCustomizable server address"

        binding.btnLogin.setOnClickListener {
            if (sharedPref.getString("server_address", "").isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please enter server address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_navigation_splash_to_navigation_login)
        }
        binding.tvServer.setOnClickListener {
            val editText = EditText(requireContext()).apply {
                setText(sharedPref.getString("server_address", ""))
                hint = "e.g., rs-address"
                inputType = InputType.TYPE_TEXT_VARIATION_URI
                typeface = ResourcesCompat.getFont(context, R.font.kanit_regular)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(32, 24, 32, 24)
                background = ContextCompat.getDrawable(context, R.drawable.bg_input_common_border_10)
            }

            val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = 32
                rightMargin = 32
            }

            editText.layoutParams = layoutParams

            AlertDialog.Builder(requireContext())
                .setTitle("Enter server address")
                .setView(editText)
                .setPositiveButton("Save") { dialog, _ ->
                    val input = editText.text.toString().trim()
                    if (input.isEmpty()) {
                        Toast.makeText(requireContext(), "Server address cannot be empty", Toast.LENGTH_SHORT).show()
                    } else {
                        sharedPref.edit().putString("server_address", input).apply()
                        binding.tvServer.text = "Server: $input\nCustomizable server address"
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}