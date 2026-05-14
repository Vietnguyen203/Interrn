package com.food.order.utils.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.food.order.R
import com.food.order.databinding.ViewSystemMenuBinding

class SystemMenuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSystemMenuBinding =
        ViewSystemMenuBinding.inflate(LayoutInflater.from(context), this)

    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SystemMenuView)
            val iconRes = typedArray.getResourceId(R.styleable.SystemMenuView_iconSrc, R.drawable.icon_food_menu)
            val title = typedArray.getString(R.styleable.SystemMenuView_titleText) ?: "System Menu"
            typedArray.recycle()

            setIconAndTitle(iconRes, title)
        }
    }

    private fun setIconAndTitle(@DrawableRes iconRes: Int, title: String) {
        binding.ivIcon.setImageResource(iconRes)
        binding.tvName.text = title
    }
}
