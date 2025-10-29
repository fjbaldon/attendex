package com.github.fjbaldon.attendex.scanner.ui.screens.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.fjbaldon.attendex.scanner.ui.camera.CameraPreview

sealed class ScanUiResult {
    data object Idle : ScanUiResult()
    data class Success(val attendeeName: String) : ScanUiResult()
    data class Error(val message: String) : ScanUiResult()
    data class AlreadyScanned(val identifier: String) : ScanUiResult()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.lastScanResult) {
        when (val result = uiState.lastScanResult) {
            is ScanUiResult.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Success: ${result.attendeeName}",
                    duration = SnackbarDuration.Short
                )
            }

            is ScanUiResult.Error -> {
                snackbarHostState.showSnackbar(
                    message = "Error: ${result.message}",
                    duration = SnackbarDuration.Short
                )
            }

            is ScanUiResult.AlreadyScanned -> { // This branch has been added
                snackbarHostState.showSnackbar(
                    message = "Already Scanned: ${result.identifier}",
                    duration = SnackbarDuration.Short
                )
            }

            is ScanUiResult.Idle -> {
                // Do nothing
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.eventName ?: "Scanner") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.hasFlashUnit) {
                        IconButton(onClick = { viewModel.onTorchToggle(!uiState.isTorchOn) }) {
                            Icon(
                                imageVector = if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Flashlight"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                CameraPreview(
                    onTextFound = { qrCode ->
                        viewModel.processQrCode(qrCode)
                    },
                    torchEnabled = uiState.isTorchOn,
                    onTorchToggle = { hasFlash -> viewModel.onFlashUnitAvailabilityChange(hasFlash) }
                )
                ScannerOverlay(result = uiState.lastScanResult)
            }
        }
    }
}

@Composable
private fun ScannerOverlay(result: ScanUiResult) {
    val (text, textColor, overlayColor) = when (result) {
        is ScanUiResult.Success -> Triple(
            "Checked-in: ${result.attendeeName}",
            Color(0xFF4CAF50),
            Color(0xFF4CAF50).copy(alpha = 0.5f)
        )

        is ScanUiResult.Error -> Triple(
            result.message,
            Color(0xFFF44336),
            Color(0xFFF44336).copy(alpha = 0.5f)
        )

        is ScanUiResult.AlreadyScanned -> Triple(
            "Already Scanned: ${result.identifier}",
            Color(0xFFFFC107),
            Color(0xFFFFC107).copy(alpha = 0.5f)
        )

        is ScanUiResult.Idle -> Triple(
            "Point camera at an ID",
            Color.White,
            Color.Transparent
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayColor)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
                .aspectRatio(2f)
                .clip(MaterialTheme.shapes.medium)
        ) {
            OutlinedCard(
                border = BorderStroke(4.dp, Color.White.copy(alpha = 0.7f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {}
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
