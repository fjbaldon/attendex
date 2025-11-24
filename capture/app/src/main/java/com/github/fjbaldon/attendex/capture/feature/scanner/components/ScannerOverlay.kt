package com.github.fjbaldon.attendex.capture.feature.scanner.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.fjbaldon.attendex.capture.feature.scanner.ScanUiResult

@Composable
fun ScannerOverlay(
    result: ScanUiResult,
    isEventActive: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (result !is ScanUiResult.Idle || !isEventActive) {

            // Using Material Theme Mappings:
            // Tertiary = Success (Green)
            // Error = Failure (Red)
            // ErrorContainer = Warning (Orange)

            val (message, color) = when {
                !isEventActive -> "Event is not active" to MaterialTheme.colorScheme.errorContainer
                result is ScanUiResult.Success -> result.attendeeDetails to MaterialTheme.colorScheme.tertiary
                result is ScanUiResult.AlreadyScanned -> result.attendeeDetails to MaterialTheme.colorScheme.tertiary
                result is ScanUiResult.NotFound -> "Not in Roster" to MaterialTheme.colorScheme.error
                result is ScanUiResult.Error -> result.message to MaterialTheme.colorScheme.error
                else -> "" to Color.Transparent
            }

            if (message.isNotEmpty()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    contentColor = color,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 170.dp)
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = message,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }
        }
    }
}
