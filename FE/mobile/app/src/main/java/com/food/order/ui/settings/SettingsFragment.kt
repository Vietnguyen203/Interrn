package com.food.order.ui.settings

import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.food.order.R
import com.food.order.data.RetrofitClient
import com.food.order.data.SessionManager
import com.food.order.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        // ── Back ──────────────────────────────────────────────
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // ── Dark Mode ─────────────────────────────────────────
        val darkEnabled = SessionManager.isDarkMode(ctx)
        binding.switchDarkMode.isChecked = darkEnabled
        binding.rowDarkMode.setOnClickListener { toggleDarkMode() }
        binding.switchDarkMode.setOnCheckedChangeListener(null) // prevent double fire

        // ── Sound ─────────────────────────────────────────────
        val soundEnabled = SessionManager.isSoundEnabled(ctx)
        binding.switchSound.isChecked = soundEnabled
        updateSoundDesc(soundEnabled)
        binding.rowSound.setOnClickListener { toggleSound() }

        // ── Language ──────────────────────────────────────────
        val lang = SessionManager.getLang(ctx)
        updateLangToggle(lang)
        binding.langToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newLang = if (checkedId == R.id.btnVI) "vi" else "en"
                if (newLang != SessionManager.getLang(ctx)) {
                    SessionManager.setLang(ctx, newLang)
                    updateLangDesc(newLang)
                    activity?.recreate()
                }
            }
        }

        // ── Server Address ────────────────────────────────────
        val role = SessionManager.getRoleOrUnknown(ctx)
        if (role == "ADMIN") {
            binding.tvSystemTitle.visibility = View.VISIBLE
            binding.cardSystem.visibility = View.VISIBLE
            val currentUrl = RetrofitClient.currentBaseUrl(ctx)
            binding.tvServerUrl.text = if (currentUrl.isNotBlank()) currentUrl
                                       else getString(R.string.server_hint_url)
            binding.rowServer.setOnClickListener {
                ServerAddressDialogFragment.newInstance().show(parentFragmentManager, "SERVER_DIALOG")
            }
        } else {
            binding.tvSystemTitle.visibility = View.GONE
            binding.cardSystem.visibility = View.GONE
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private fun toggleDarkMode() {
        val ctx = requireContext()
        val newVal = !SessionManager.isDarkMode(ctx)
        SessionManager.setDarkMode(ctx, newVal)
        binding.switchDarkMode.isChecked = newVal
        AppCompatDelegate.setDefaultNightMode(
            if (newVal) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun toggleSound() {
        val ctx = requireContext()
        val newVal = !SessionManager.isSoundEnabled(ctx)
        SessionManager.setSoundEnabled(ctx, newVal)
        binding.switchSound.isChecked = newVal
        updateSoundDesc(newVal)
        // Preview sound when enabled
        if (newVal) playPreviewSound()
    }

    private fun updateSoundDesc(enabled: Boolean) {
        binding.tvSoundDesc.text = if (enabled)
            getString(R.string.sound_notification_desc_on)
        else
            getString(R.string.sound_notification_desc_off)
    }

    private fun updateLangToggle(lang: String) {
        val targetId = if (lang == "vi") R.id.btnVI else R.id.btnEN
        // Check without triggering listener
        if (binding.langToggleGroup.checkedButtonId != targetId) {
            binding.langToggleGroup.check(targetId)
        }
        updateLangDesc(lang)
    }

    private fun updateLangDesc(lang: String) {
        binding.tvLangDesc.text = if (lang == "vi") "Tiếng Việt" else "English"
    }

    private fun playPreviewSound() {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
        } catch (_: Throwable) { /* device may not support */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
