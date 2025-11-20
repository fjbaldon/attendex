package com.github.fjbaldon.attendex.capture.core.ui.camera

import android.graphics.Rect
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionAnalyzer(
    private val onTextFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val numberRegex = "\\d+".toRegex()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        // Calculate the "Hot Zone" (The center 70% of the image)
        // This matches the visual overlay we will draw
        val width = imageProxy.width
        val height = imageProxy.height

        // We define a rect in the middle of the image coordinates
        val scanAreaSize = 0.7f
        val rectWidth = (width * scanAreaSize).toInt()
        val rectHeight = (rectWidth * 0.6).toInt() // Aspect ratio matching overlay

        val left = (width - rectWidth) / 2
        val top = (height - rectHeight) / 2
        val right = left + rectWidth
        val bottom = top + rectHeight

        val activeScanRect = Rect(left, top, right, bottom)

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks) {
                    val boundingBox = block.boundingBox ?: continue

                    // LOGIC FIX: Only process text if its center is inside the Hot Zone
                    if (activeScanRect.contains(boundingBox.centerX(), boundingBox.centerY())) {
                        val blockText = block.text
                        // We look for numeric IDs
                        val match = numberRegex.find(blockText)
                        if (match != null && match.value.isNotBlank()) {
                            onTextFound(match.value)
                            // Stop after finding the first valid centered code
                            return@addOnSuccessListener
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure if needed
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
