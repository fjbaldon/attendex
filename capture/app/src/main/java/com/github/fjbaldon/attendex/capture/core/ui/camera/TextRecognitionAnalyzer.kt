package com.github.fjbaldon.attendex.capture.core.ui.camera

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

class TextRecognitionAnalyzer(
    private val onTextFound: (String) -> Unit,
    private val isScanningEnabledRef: AtomicBoolean,
    private val customRegexString: String? // NEW PARAMETER
) : BaseImageAnalyzer(isScanningEnabledRef, throttleIntervalMs = 20L) {

    // 1. Determine the regex to use.
    // If custom is provided, we use it. Otherwise, default to 5+ digits.
    private val activeRegex = try {
        if (!customRegexString.isNullOrBlank()) {
            customRegexString.toRegex()
        } else {
            "\\d{5,}".toRegex()
        }
    } catch (_: Exception) {
        // Fallback if the user typed an invalid regex in the dashboard
        "\\d{5,}".toRegex()
    }

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var lastSeenText: String? = null
    private var stableFrameCount = 0
    private val requiredStableFrames = 2

    override fun processImage(image: InputImage, imageProxy: ImageProxy) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (isScanningEnabledRef.get()) {
                    var foundMatch = false

                    // Find largest match in the image
                    val bestMatch = visionText.textBlocks
                        .flatMap { it.lines }
                        .mapNotNull { activeRegex.find(it.text)?.value }
                        .maxByOrNull { it.length }

                    if (bestMatch != null) {
                        foundMatch = true

                        // --- THE SPEED UP LOGIC ---
                        // If we have a custom regex (e.g. ^\d{9}$), we trust it more.
                        // We skip the stability check and trigger immediately.
                        val isCustomRegex = !customRegexString.isNullOrBlank()

                        if (isCustomRegex) {
                            // FAST TRACK: Instant trigger
                            onTextFound(bestMatch)
                        } else {
                            // SLOW TRACK (Default): Wait for stability to prevent noise
                            if (bestMatch == lastSeenText) {
                                stableFrameCount++
                                if (stableFrameCount >= requiredStableFrames) {
                                    onTextFound(bestMatch)
                                }
                            } else {
                                lastSeenText = bestMatch
                                stableFrameCount = 1
                            }
                        }
                    }

                    if (!foundMatch) {
                        stableFrameCount = 0
                        lastSeenText = null
                    }
                }
            }
            .addOnFailureListener { }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
