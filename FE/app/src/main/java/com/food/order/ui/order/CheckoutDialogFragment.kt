package com.food.order.ui.order

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.food.order.R
import com.food.order.data.request.CheckoutRequest
import com.food.order.databinding.DialogCheckoutBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

class CheckoutDialogFragment : DialogFragment() {

    private var _binding: DialogCheckoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderTableViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, 0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCheckoutBinding.inflate(requireActivity().layoutInflater)

        // Spinner phương thức thanh toán
        val methodAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.payment_methods,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spPaymentMethod.adapter = methodAdapter

        // Gọi preview bill ban đầu (không giảm giá)
        viewModel.previewBill(userToken(), null)

        // Lắng nghe flow để cập nhật UI / kết quả
        collectFlows()

        // Input listeners
        binding.edtDiscount.addTextChangedListener {
            val discount = binding.edtDiscount.text.toString().toDoubleOrNull()
            viewModel.previewBill(userToken(), discount)
        }
        binding.edtAmountReceived.addTextChangedListener {
            updateChangeText()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(getString(R.string.pay_title))
            .setPositiveButton(R.string.action_save, null) // set sau để validate
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val total = currentTotal() ?: run {
                    Toast.makeText(requireContext(), "Chưa có tạm tính", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val amount = binding.edtAmountReceived.text.toString().toDoubleOrNull()
                if (amount == null || amount < total) {
                    Toast.makeText(requireContext(), "Tiền khách đưa không đủ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val discount = binding.edtDiscount.text.toString().toDoubleOrNull()
                val method = binding.spPaymentMethod.selectedItem?.toString() ?: "CASH"
                val note = binding.edtNote.text?.toString()?.takeIf { it.isNotBlank() }

                viewModel.checkout(
                    userToken(),
                    CheckoutRequest(
                        paymentMethod = method,
                        amountReceived = amount,
                        discount = discount,
                        note = note
                    )
                )
                // Dismiss sau khi nhận kết quả ở collectFlows()
            }
        }

        return dialog
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Cập nhật subtotal/discount/total khi preview thay đổi
                launch {
                    viewModel.billPreviewFlow.collect { res ->
                        val p = res.data
                        binding.tvSubtotal.text = getString(R.string.pay_subtotal_0)
                            .replace("0 ₫", money(p.subtotal))
                        binding.tvDiscount.text = getString(R.string.pay_discount_0)
                            .replace("0 ₫", money(p.discount))
                        binding.tvTotal.text = getString(R.string.pay_total_0)
                            .replace("0 ₫", money(p.total))
                        updateChangeText()
                    }
                }
                // Khi checkout xong (không cần giá trị cụ thể -> tránh lỗi infer type)
                launch {
                    viewModel.checkoutFlow.collect { _ ->
                        Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                        dismissAllowingStateLoss()
                    }
                }
                // Lỗi chung
                launch {
                    viewModel.errorFlow.collect { msg ->
                        if (!msg.isNullOrBlank()) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateChangeText() {
        val total = currentTotal() ?: 0.0
        val amount = binding.edtAmountReceived.text.toString().toDoubleOrNull() ?: 0.0
        val change = max(0.0, amount - total)
        binding.tvChange.text = getString(R.string.pay_change_0).replace("0 ₫", money(change))
    }

    private fun currentTotal(): Double? {
        return viewModel
            .billPreviewFlow
            .replayCache
            .lastOrNull()
            ?.data
            ?.total
    }

    private fun money(v: Double?): String {
        val nf = NumberFormat.getInstance(Locale("vi", "VN"))
        return nf.format(v ?: 0.0) + " ₫"
    }

    private fun userToken(): String {
        return requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): CheckoutDialogFragment = CheckoutDialogFragment()
    }
}
