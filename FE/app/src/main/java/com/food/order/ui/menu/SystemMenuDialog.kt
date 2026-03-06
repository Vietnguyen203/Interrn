package com.food.order.ui.menu

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.food.order.R

/**
 * System menu (layout tùy biến) – đã:
 * 1) GỠ mục Order khỏi chức năng (ẩn GONE nếu có trong layout).
 * 2) Staff luôn mở được (không check quyền).
 */
class SystemMenuDialog private constructor(
    private val onSelect: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Dùng theme mặc định để tránh phụ thuộc style ngoài
        return Dialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_system_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Gắn click cho các mục còn lại ---
        view.findViewById<View>(R.id.menuViewDashboard)?.setOnClickListener {
            onSelect("DASHBOARD"); dismiss()
        }
        view.findViewById<View>(R.id.menuViewFoodMenu)?.setOnClickListener {
            onSelect("FOOD_MENU"); dismiss()
        }
        view.findViewById<View>(R.id.menuViewOrderManagement)?.setOnClickListener {
            onSelect("ORDER_MANAGEMENT"); dismiss()
        }
        view.findViewById<View>(R.id.menuViewTables)?.setOnClickListener {
            onSelect("TABLES"); dismiss()
        }
        // Staff: không kiểm tra role – ai cũng vào được
        view.findViewById<View>(R.id.menuViewStaff)?.setOnClickListener {
            onSelect("STAFF"); dismiss()
        }
        view.findViewById<View>(R.id.menuViewReport)?.setOnClickListener {
            onSelect("REPORTS"); dismiss()
        }
        view.findViewById<View>(R.id.menuViewLogout)?.setOnClickListener {
            onSelect("LOGOUT"); dismiss()
        }
        view.findViewById<View>(R.id.menuViewSettings)?.setOnClickListener {
            onSelect("SETTINGS"); dismiss()
        }

        // --- ẨN HẲN ô Order (nếu có trong layout) ---
        view.findViewById<View>(R.id.menuViewOrder)?.apply {
            visibility = View.GONE
            setOnClickListener(null)
        }
    }

    override fun onStart() {
        super.onStart()
        // Rộng ~ 92% màn hình cho giống card cũ
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance(onSelect: (String) -> Unit): SystemMenuDialog =
            SystemMenuDialog(onSelect)
    }
}
