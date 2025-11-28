package com.github.fjbaldon.attendex.capture.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

fun ticketShape(cornerRadius: Float, notchRadius: Float) = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    // Notch position (72% down)
    val notchY = height * 0.72f

    reset()
    // 1. Top Left Corner
    moveTo(0f, cornerRadius)
    arcTo(
        rect = Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
        startAngleDegrees = 180f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )

    // 2. Top Edge to Top Right Corner
    lineTo(width - cornerRadius, 0f)
    arcTo(
        rect = Rect(width - cornerRadius * 2, 0f, width, cornerRadius * 2),
        startAngleDegrees = 270f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )

    // 3. Right Edge down to Notch
    lineTo(width, notchY - notchRadius)
    // RIGHT NOTCH (Draw inward arc)
    arcTo(
        rect = Rect(width - notchRadius, notchY - notchRadius, width + notchRadius, notchY + notchRadius),
        startAngleDegrees = 270f,
        sweepAngleDegrees = -180f, // Counter-clockwise for inward cut
        forceMoveTo = false
    )

    // 4. Right Edge to Bottom Right Corner
    lineTo(width, height - cornerRadius)
    arcTo(
        rect = Rect(width - cornerRadius * 2, height - cornerRadius * 2, width, height),
        startAngleDegrees = 0f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )

    // 5. Bottom Edge to Bottom Left Corner
    lineTo(cornerRadius, height)
    arcTo(
        rect = Rect(0f, height - cornerRadius * 2, cornerRadius * 2, height),
        startAngleDegrees = 90f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )

    // 6. Left Edge up to Notch
    lineTo(0f, notchY + notchRadius)
    // LEFT NOTCH (Draw inward arc)
    arcTo(
        rect = Rect(-notchRadius, notchY - notchRadius, notchRadius, notchY + notchRadius),
        startAngleDegrees = 90f,
        sweepAngleDegrees = -180f, // Counter-clockwise for inward cut
        forceMoveTo = false
    )

    // 7. Close Path
    lineTo(0f, cornerRadius)
    close()
}

@Composable
fun TicketDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    dotSize: Float = 10f,
    gapSize: Float = 10f
) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dotSize, gapSize), 0f)
        )
    }
}
