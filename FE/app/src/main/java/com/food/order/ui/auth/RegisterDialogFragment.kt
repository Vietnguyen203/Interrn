package com.food.order.ui.auth

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.food.order.R
import com.food.order.data.RetrofitClient
import com.food.order.data.SessionManager
import com.food.order.data.request.LoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_register, null, false)
        dialog.setContentView(view)

        val edtUser  = view.findViewById<EditText>(R.id.edtUsername)
        val edtPass  = view.findViewById<EditText>(R.id.edtPassword)
        val edtPass2 = view.findViewById<EditText>(R.id.edtPassword2)
        val btnOk    = view.findViewById<Button>(R.id.btnRegister)
        val btnCancel= view.findViewById<Button>(R.id.btnCancel)

        btnCancel.setOnClickListener { dismiss() }

        btnOk.setOnClickListener {
            val employeeId  = edtUser.text.toString().trim()
            val password    = edtPass.text.toString().trim()
            val password2   = edtPass2.text.toString().trim()
            val server      = "server-1" // TODO: cho người dùng nhập nếu cần

            when {
                employeeId.isEmpty()   -> { edtUser.error = getString(R.string.err_required); return@setOnClickListener }
                password.length < 6    -> { edtPass.error = getString(R.string.err_pass_len); return@setOnClickListener }
                password != password2  -> { edtPass2.error = getString(R.string.err_pass_match); return@setOnClickListener }
            }

            lifecycleScope.launch {
                val api = RetrofitClient.instance
                try {
                    // 1) Tự đăng ký (public)
                    val body = mapOf(
                        "employeeId" to employeeId,
                        "password"   to password,
                        "server"     to server
                    )
                    val reg = api.registerPublic(body) // TokenResponse

                    // 2) Lấy token: nếu register trả về thì dùng, ngược lại fallback login
                    var token = reg.data?.token.orEmpty()
                    if (token.isBlank()) {
                        val login = api.login(
                            LoginRequest(
                                username = null,
                                employeeId = employeeId,
                                password   = password,
                                server     = server
                            )
                        )
                        token = login.data?.token.orEmpty()
                    }

                    if (token.isBlank()) {
                        Toast.makeText(requireContext(), "Register/Login failed", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    // 3) Lưu RAW token (không thêm "Bearer " ở đây)
                    SessionManager.saveToken(requireContext(), token)

                    Toast.makeText(requireContext(), getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                    dismiss()

                } catch (e: HttpException) {
                    Toast.makeText(
                        requireContext(),
                        "Register failed (${e.code()} ${e.message()})",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message ?: "Unexpected error", Toast.LENGTH_LONG).show()
                }
            }
        }

        return dialog
    }

    companion object { fun newInstance() = RegisterDialogFragment() }
}
