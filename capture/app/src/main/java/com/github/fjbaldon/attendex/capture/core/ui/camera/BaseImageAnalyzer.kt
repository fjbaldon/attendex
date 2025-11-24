package com.github.fjbaldon.attendex.capture.core.ui.camera

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseImageAnalyzer(
    private val isScanningEnabled: AtomicBoolean,
    private val throttleIntervalMs: Long
) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // 1. Fast exit if disabled
        if (!isScanningEnabled.get()) {
            imageProxy.close()
            return
        }

        // 2. Throttle FPS using monotonic time
        // FIXED: Replaced System.currentTimeMillis() with SystemClock.elapsedRealtime()
        val currentTimestamp = SystemClock.elapsedRealtime()
        if (currentTimestamp - lastAnalyzedTimestamp < throttleIntervalMs) {
            imageProxy.close()
            return
        }
        lastAnalyzedTimestamp = currentTimestamp

        // 3. Prepare Image
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // 4. Delegate to subclass
            processImage(image, imageProxy)
        } else {
            imageProxy.close()
        }
    }

    /**
     * Subclasses must implement this.
     * IMPORTANT: Subclasses MUST close the imageProxy when done.
     */
    protected abstract fun processImage(image: InputImage, imageProxy: ImageProxy)
}
