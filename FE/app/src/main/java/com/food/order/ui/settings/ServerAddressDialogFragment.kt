package com.food.order.ui.settings

import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.food.order.R
import com.food.order.data.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ServerAddressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_server_address, null, false)
        dialog.setContentView(view)

        val edt = view.findViewById<EditText>(R.id.editServer)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val hint = view.findViewById<TextView>(R.id.tvHint)
        hint.movementMethod = LinkMovementMethod.getInstance()

        // Prefill BASE_URL hiện tại
        edt.setText(RetrofitClient.currentBaseUrl(requireContext()))
        // (tuỳ chọn) bạn có thể show X-Server hiện tại cho dễ nhìn:
        // hint.append("\n• X-Server: ${RetrofitClient.currentXServer(requireContext())}")

        btnSave.setOnClickListener {
            val raw = edt.text.toString().trim()

            // Nếu người dùng nhập token (server-1/local/...) -> coi là X-Server
            if (raw.matches(Regex("^[A-Za-z0-9_-]+$"))) {
                RetrofitClient.setXServer(requireContext(), raw)
                Toast.makeText(requireContext(), "Đã lưu X-Server = $raw", Toast.LENGTH_LONG).show()
                dismiss()
                return@setOnClickListener
            }

            val normalized = normalizeBaseUrl(raw)
            edt.setText(normalized)

            viewLifecycleOwner.lifecycleScope.launch {
                val originalText = btnSave.text.toString()
                btnSave.isEnabled = false
                btnCancel.isEnabled = false
                btnSave.text = getString(R.string.checking)

                val ok = isReachable(normalized)
                if (ok) {
                    RetrofitClient.rebuild(requireContext(), normalized)
                    Toast.makeText(requireContext(), "Server saved: $normalized", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Không kết nối được server (/health).", Toast.LENGTH_LONG).show()
                }

                btnSave.text = getString(R.string.save)
                btnSave.isEnabled = true
                btnCancel.isEnabled = true
            }
        }

        btnCancel.setOnClickListener { dismiss() }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /** Chuẩn hoá URL và tự thêm context-path nếu thiếu */
    private fun normalizeBaseUrl(raw: String): String {
        var s = raw.trim()
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://$s"
        s = s.replace("\\", "/")
        s = s.replace(Regex("(?<!:)/{2,}"), "/")
        if (!s.endsWith("/")) s += "/"
        if (!s.contains("/foodordersystem/api/")) {
            s += "foodordersystem/api/"
        }
        return s
    }

    private suspend fun isReachable(baseUrl: String): Boolean = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        fun tryUrl(path: String) = try {
            val req = Request.Builder().url(baseUrl + path).get().build()
            client.newCall(req).execute().use { it.isSuccessful }
        } catch (_: Throwable) { false }

        tryUrl("health") || tryUrl("actuator/health")
    }

    companion object {
        fun newInstance() = ServerAddressDialogFragment()
    }
}
