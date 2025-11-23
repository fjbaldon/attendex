package com.github.fjbaldon.attendex.capture.core.ui.camera

import android.graphics.Rect
import androidx.annotation.OptIn
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
        val width = imageProxy.width
        val height = imageProxy.height

        val scanAreaSize = 0.7f
        val rectWidth = (width * scanAreaSize).toInt()
        val rectHeight = (rectWidth * 0.6).toInt()

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

                    if (activeScanRect.contains(boundingBox.centerX(), boundingBox.centerY())) {
                        val blockText = block.text
                        val match = numberRegex.find(blockText)
                        if (match != null && match.value.isNotBlank()) {
                            onTextFound(match.value)
                            return@addOnSuccessListener
                        }
                    }
                }
            }
            .addOnFailureListener { }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
