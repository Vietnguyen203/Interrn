package com.food.order.utils.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.food.order.databinding.ViewFoodOrderLoadingBinding

class FoodOrderLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewFoodOrderLoadingBinding =
        ViewFoodOrderLoadingBinding.inflate(LayoutInflater.from(context), this)

    init {
        isVisible = false
    }

    fun show() {
        isVisible = true
        binding.lottie.playAnimation()
    }

    fun hide() {
        binding.lottie.pauseAnimation()
        isVisible = false
    }

    fun isShowing(): Boolean = isVisible
}