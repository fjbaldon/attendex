package com.github.fjbaldon.attendex.capture.core.services

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsService @Inject constructor(
    @param:ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            // We use the default engine.
            // Note: In rare cases, passing "com.google.android.tts" explicitly helps,
            // but usually default is safer across different manufacturers.
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("TTS", "Failed to initialize TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val targetLocale = Locale.US

            // 1. Always set Audio Attributes first
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY) // Higher priority than MEDIA
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(attr)

            // 2. Basic Language Check
            val result = tts?.setLanguage(targetLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language missing data. Attempting to download...")
                // Even if missing, we mark ready = true so it might work if network comes back
                // or if the OS was lying (common bug).
            }

            // 3. Smart Offline Voice Selection
            // We try to find a voice that is NOT network required.
            // If we find one, we force it. If not, we stick with the default language set above.
            try {
                val offlineVoice = tts?.voices?.firstOrNull {
                    it.locale == targetLocale && !it.isNetworkConnectionRequired
                }

                if (offlineVoice != null) {
                    tts?.voice = offlineVoice
                    Log.i("TTS", "Using optimized offline voice: ${offlineVoice.name}")
                } else {
                    Log.w("TTS", "No specific offline voice found. Using system default for US English.")
                }
            } catch (e: Exception) {
                Log.w("TTS", "Failed to list voices, sticking to default language", e)
            }

            // CRITICAL FIX: Always set ready to true if initialization succeeded.
            // Do not disable it just because a voice check failed.
            isTtsReady = true
        } else {
            Log.e("TTS", "Init Failed with status $status")
            isTtsReady = false

            // Optional: Retry logic could go here
        }
    }

    fun speak(text: String) {
        if (!isTtsReady || tts == null) {
            Log.w("TTS", "TTS not ready, re-initializing...")
            initializeTts() // Try to revive it
            return
        }

        try {
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SCAN_ID")

            if (result == TextToSpeech.ERROR) {
                Log.e("TTS", "Engine returned ERROR during speak")
            }
        } catch (e: Exception) {
            Log.e("TTS", "Speak threw exception", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) { }
        tts = null
        isTtsReady = false
    }
}
