package com.github.fjbaldon.attendex.capture.ui.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionAnalyzer(
    private val onTextFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val numberRegex = "\\d+".toRegex()

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks) {
                    val blockText = block.text
                    val match = numberRegex.find(blockText)
                    if (match != null && match.value.isNotBlank()) {
                        onTextFound(match.value)
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener {
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
