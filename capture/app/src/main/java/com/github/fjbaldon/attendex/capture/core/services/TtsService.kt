package com.github.fjbaldon.attendex.capture.core.services

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

    @Volatile
    private var currentOnDone: (() -> Unit)? = null

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("TTS", "Failed to initialize TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val targetLocale = Locale.US

            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            tts?.setAudioAttributes(attr)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // No-op
                }

                override fun onDone(utteranceId: String?) {
                    handleCallback()
                }

                // 1. MANDATORY (Abstract): Must be implemented to compile, but marked deprecated
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.w("TTS", "Error occurred (Legacy)")
                    handleCallback()
                }

                // 2. PREFERRED (API 21+): This is the non-deprecated method called by modern Android
                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e("TTS", "Error occurred: Code $errorCode")
                    handleCallback()
                }

                // Shared cleanup helper
                private fun handleCallback() {
                    currentOnDone?.invoke()
                    currentOnDone = null
                }
            })

            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language missing/not supported")
            } else {
                // Optimization: Try to find an offline voice, fallback to default if not found
                try {
                    val voice = tts?.voices?.firstOrNull {
                        it.locale == targetLocale && !it.isNetworkConnectionRequired
                    }
                    if (voice != null) tts?.voice = voice
                } catch (_: Exception) {}
            }

            isTtsReady = true
        } else {
            Log.e("TTS", "Init Failed with status $status")
            isTtsReady = false
        }
    }

    fun speak(text: String, onDone: () -> Unit = {}) {
        if (!isTtsReady || tts == null) {
            onDone()
            return
        }

        this.currentOnDone = onDone

        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString())

        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "SCAN_ID")

        if (result == TextToSpeech.ERROR) {
            Log.e("TTS", "Error queuing text")
            onDone()
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) { }
        tts = null
        currentOnDone = null
    }
}
