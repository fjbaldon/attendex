package com.github.fjbaldon.attendex.capture.core.ui.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun CameraOverlay(
    modifier: Modifier = Modifier
) {
    // Animation for the red laser line
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val animatedLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "line_position"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Match these dimensions with TextRecognitionAnalyzer
        val scanAreaSize = 0.7f // 70% width
        val boxWidth = canvasWidth * scanAreaSize
        val boxHeight = boxWidth * 0.6f // Aspect ratio typical for ID cards

        val left = (canvasWidth - boxWidth) / 2
        val top = (canvasHeight - boxHeight) / 2
        val right = left + boxWidth
        val bottom = top + boxHeight

        // 1. Draw Darkened Background with "Hole"
        with(drawContext.canvas.nativeCanvas) {
            val checkPoint = saveLayer(null, null)

            // Dim everything
            drawRect(Color.Black.copy(alpha = 0.6f))

            // Cut out the center (Clear mode)
            drawRoundRect(
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(16f, 16f),
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )

            restoreToCount(checkPoint)
        }

        // 2. Draw White Border Corners
        drawRoundRect(
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16f, 16f),
            color = Color.White,
            style = Stroke(width = 6f)
        )

        // 3. Draw Red Laser Line
        val lineY = top + (boxHeight * animatedLineY)

        // Add a fade effect to the ends of the line
        drawLine(
            color = Color.Red.copy(alpha = 0.8f),
            start = Offset(left + 20f, lineY),
            end = Offset(right - 20f, lineY),
            strokeWidth = 4f
        )
    }
}
