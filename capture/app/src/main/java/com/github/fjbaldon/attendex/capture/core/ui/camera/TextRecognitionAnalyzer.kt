package com.github.fjbaldon.attendex.capture.core.ui.camera

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

class TextRecognitionAnalyzer(
    private val onTextFound: (String) -> Unit,
    private val isScanningEnabledRef: AtomicBoolean,
    customRegexString: String?
) : BaseImageAnalyzer(isScanningEnabledRef, throttleIntervalMs = 50L) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val activeRegex = try {
        if (!customRegexString.isNullOrBlank()) customRegexString.toRegex() else "\\d{5,}".toRegex()
    } catch (_: Exception) { "\\d{5,}".toRegex() }

    @OptIn(ExperimentalGetImage::class)
    override fun processImage(image: InputImage, imageProxy: ImageProxy) {
        val imgWidth = imageProxy.width
        val imgHeight = imageProxy.height

        // 1. ROI: Ignore top and bottom 35%
        val cropRect = Rect(
            0,
            (imgHeight * 0.35).toInt(),
            imgWidth,
            (imgHeight * 0.65).toInt()
        )

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (!isScanningEnabledRef.get()) return@addOnSuccessListener

                fun tryMatch(rawText: String, box: Rect?): Boolean {
                    // A. Spatial Check
                    if (box != null && !Rect.intersects(box, cropRect)) return false

                    val trimmed = rawText.trim()

                    // B. Fuzzy Logic
                    val isNumericMode = activeRegex.pattern.contains("\\d") && !activeRegex.pattern.contains("[a-zA-Z]")
                    val cleanText = if (isNumericMode) {
                        trimmed.replace("O", "0")
                            .replace("o", "0")
                            .replace("l", "1")
                            .replace("I", "1")
                            .replace(" ", "")
                    } else {
                        trimmed
                    }

                    // C. Match
                    if (activeRegex.matches(cleanText)) {
                        onTextFound(cleanText)
                        return true
                    }
                    return false
                }

                // 2. Separate Loops to avoid Type Erasure Error
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        if (tryMatch(line.text, line.boundingBox)) return@addOnSuccessListener
                        for (element in line.elements) {
                            if (tryMatch(element.text, element.boundingBox)) return@addOnSuccessListener
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
