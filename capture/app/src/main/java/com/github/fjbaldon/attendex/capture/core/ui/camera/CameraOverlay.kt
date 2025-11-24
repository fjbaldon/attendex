package com.github.fjbaldon.attendex.capture.core.ui.camera

import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.github.fjbaldon.attendex.capture.feature.scanner.ScanMode
import com.github.fjbaldon.attendex.capture.feature.scanner.ScanUiResult
import kotlinx.coroutines.launch

@Composable
fun CameraOverlay(
    scanMode: ScanMode,
    scanResult: ScanUiResult,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val animatedLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "line_position"
    )

    val aspectRatio by animateFloatAsState(
        targetValue = if (scanMode == ScanMode.QR) 1.0f else 0.6f,
        label = "box_aspect_ratio"
    )

    // === THEME COLORS ===
    // Map semantic meanings to Material Theme slots
    val successColor = MaterialTheme.colorScheme.tertiary // Green
    val errorColor = MaterialTheme.colorScheme.error       // Red
    val defaultColor = Color.White

    // === ANIMATION STATE ===
    val borderWidth = remember { Animatable(6f) }
    val echoAlpha = remember { Animatable(0f) }
    val echoScale = remember { Animatable(1f) }

    val borderColor = remember {
        Animatable(
            initialValue = defaultColor,
            typeConverter = Color.VectorConverter(defaultColor.colorSpace)
        )
    }

    LaunchedEffect(scanResult) {
        val isGreenState = scanResult is ScanUiResult.Success || scanResult is ScanUiResult.AlreadyScanned
        val isRedState = scanResult is ScanUiResult.NotFound || scanResult is ScanUiResult.Error

        if (isGreenState) {
            // Reset
            borderColor.snapTo(defaultColor)
            borderWidth.snapTo(6f)
            echoAlpha.snapTo(0.8f)
            echoScale.snapTo(1f)

            // 1. Main Color Fade
            launch {
                borderColor.animateTo(
                    targetValue = successColor,
                    animationSpec = tween(400)
                )
            }

            // 2. Main Border Fluid Pulse
            launch {
                borderWidth.animateTo(
                    targetValue = 60f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                )
                borderWidth.animateTo(
                    targetValue = 6f,
                    animationSpec = spring(
                        dampingRatio = 0.5f,
                        stiffness = 200f
                    )
                )
            }

            // 3. Echo Ripple
            launch {
                launch {
                    echoScale.animateTo(1.4f, tween(600, easing = LinearOutSlowInEasing))
                }
                launch {
                    echoAlpha.animateTo(0f, tween(600))
                }
            }

        } else if (isRedState) {
            borderColor.animateTo(errorColor, tween(100))
            borderWidth.animateTo(6f, tween(100))
        } else {
            borderColor.animateTo(defaultColor, tween(300))
            borderWidth.animateTo(6f, tween(300))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val scanAreaSize = 0.7f
        val boxWidth = canvasWidth * scanAreaSize
        val boxHeight = boxWidth * aspectRatio

        val left = (canvasWidth - boxWidth) / 2
        val top = (canvasHeight - boxHeight) / 2
        val right = left + boxWidth

        // === ECHO RIPPLE ===
        if (echoAlpha.value > 0f) {
            val echoWidth = boxWidth * echoScale.value
            val echoHeight = boxHeight * echoScale.value
            val echoLeft = (canvasWidth - echoWidth) / 2
            val echoTop = (canvasHeight - echoHeight) / 2

            drawRoundRect(
                topLeft = Offset(echoLeft, echoTop),
                size = Size(echoWidth, echoHeight),
                cornerRadius = CornerRadius(16f * echoScale.value, 16f * echoScale.value),
                color = borderColor.value.copy(alpha = echoAlpha.value),
                style = Stroke(width = 4f)
            )
        }

        // === MAIN PULSING BORDER ===
        drawRoundRect(
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16f, 16f),
            color = borderColor.value,
            style = Stroke(width = borderWidth.value)
        )

        // Laser Line
        val lineY = top + (boxHeight * animatedLineY)
        drawLine(
            color = borderColor.value.copy(alpha = 0.8f),
            start = Offset(left + 20f, lineY),
            end = Offset(right - 20f, lineY),
            strokeWidth = 4f
        )
    }
}
