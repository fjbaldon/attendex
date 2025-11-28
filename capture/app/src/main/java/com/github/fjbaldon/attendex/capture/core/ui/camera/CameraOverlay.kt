package com.github.fjbaldon.attendex.capture.core.ui.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.github.fjbaldon.attendex.capture.feature.scanner.ScanMode
import com.github.fjbaldon.attendex.capture.feature.scanner.ScanUiResult

@Composable
fun CameraOverlay(
    scanMode: ScanMode,
    scanResult: ScanUiResult,
    modifier: Modifier = Modifier
) {
    // 1. Capture Theme Colors (Must be done in Composable scope, not DrawScope)
    val successColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val laserColor = MaterialTheme.colorScheme.primary

    // 2. Determine State Colors
    val targetColor = when (scanResult) {
        is ScanUiResult.Success, is ScanUiResult.AlreadyScanned -> successColor
        is ScanUiResult.NotFound, is ScanUiResult.Error -> errorColor
        else -> Color.White
    }

    // Smooth color transition
    val borderColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "hud_color"
    )

    // Pulse Animation for idle/scanning state
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "hud_alpha"
    )

    // Scan Line Animation (Laser)
    val animatedLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "laser_pos"
    )

    val aspectRatio = if (scanMode == ScanMode.QR) 1.0f else 0.6f

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define Box Dimensions
        val scanAreaSize = 0.75f // Slightly larger for comfort
        val boxWidth = canvasWidth * scanAreaSize
        val boxHeight = boxWidth * aspectRatio

        // FIX: Dynamic Vertical Shift
        // When in QR mode (taller box), shift it up further so the bottom edge
        // stays roughly where it is in OCR mode.
        // Difference in height = boxWidth * (1.0 - 0.6) = 0.4 * boxWidth
        // To keep bottom constant, we shift center up by half that difference = 0.2 * boxWidth
        val baseVerticalShift = canvasHeight * 0.15f
        val extraShift = if (scanMode == ScanMode.QR) boxWidth * 0.2f else 0f
        val verticalShift = baseVerticalShift + extraShift

        val left = (canvasWidth - boxWidth) / 2
        val top = (canvasHeight - boxHeight) / 2 - verticalShift
        val right = left + boxWidth
        val bottom = top + boxHeight

        // HUD Settings
        val cornerLength = 60f // Length of the brackets
        val strokeW = 12f      // Thickness of the brackets

        // Color with pulse
        val drawColor = borderColor.copy(alpha = if (scanResult is ScanUiResult.Idle) pulseAlpha else 1f)

        // --- DRAW SNIPER CORNERS ---

        // Top Left
        drawLine(drawColor, Offset(left, top), Offset(left + cornerLength, top), strokeW, StrokeCap.Round)
        drawLine(drawColor, Offset(left, top), Offset(left, top + cornerLength), strokeW, StrokeCap.Round)

        // Top Right
        drawLine(drawColor, Offset(right, top), Offset(right - cornerLength, top), strokeW, StrokeCap.Round)
        drawLine(drawColor, Offset(right, top), Offset(right, top + cornerLength), strokeW, StrokeCap.Round)

        // Bottom Left
        drawLine(drawColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeW, StrokeCap.Round)
        drawLine(drawColor, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeW, StrokeCap.Round)

        // Bottom Right
        drawLine(drawColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeW, StrokeCap.Round)
        drawLine(drawColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeW, StrokeCap.Round)

        // --- DRAW LASER (Only when idle to imply scanning) ---
        if (scanResult is ScanUiResult.Idle) {
            val lineY = top + (boxHeight * animatedLineY)
            drawLine(
                color = laserColor.copy(alpha = 0.5f), // Fixed: Uses captured variable
                start = Offset(left + 20f, lineY),
                end = Offset(right - 20f, lineY),
                strokeWidth = 2f
            )
        }
    }
}
