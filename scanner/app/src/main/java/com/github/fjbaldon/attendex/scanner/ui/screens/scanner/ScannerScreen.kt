package com.github.fjbaldon.attendex.scanner.ui.screens.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity
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
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(uiState.lastScanResult) {
        when (val result = uiState.lastScanResult) {
            is ScanUiResult.Error -> snackbarHostState.showSnackbar("Error: ${result.message}")
            is ScanUiResult.AlreadyScanned -> snackbarHostState.showSnackbar("Already Scanned: ${result.identifier}")
            else -> {}
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            ScannedAttendeesSheetContent(
                attendees = uiState.scannedAttendees,
                modifier = Modifier.fillMaxWidth()
            )
        },
        sheetPeekHeight = 64.dp,
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    onTextFound = { qrCode -> viewModel.processQrCode(qrCode) },
                    torchEnabled = uiState.isTorchOn,
                    onTorchToggle = { hasFlash -> viewModel.onFlashUnitAvailabilityChange(hasFlash) }
                )
                ScannerOverlay(result = uiState.lastScanResult)
            }
        }
    }
}

@Composable
private fun ScannedAttendeesSheetContent(
    attendees: List<AttendeeEntity>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                .align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Scanned Attendees", style = MaterialTheme.typography.titleMedium)
            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                Text(
                    text = attendees.size.toString(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        if (attendees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No attendees scanned yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(attendees, key = { it.localId }) { attendee ->
                    ScannedAttendeeItem(attendee)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ScannedAttendeeItem(attendee: AttendeeEntity) {
    ListItem(
        headlineContent = { Text(attendee.uniqueIdentifier, fontWeight = FontWeight.Medium) },
        supportingContent = { Text("ID: ${attendee.attendeeId}") }
    )
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

        is ScanUiResult.Idle -> Triple("Point camera at a QR code", Color.White, Color.Transparent)
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
        ) {
            OutlinedCard(
                border = BorderStroke(4.dp, Color.White.copy(alpha = 0.7f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxSize()
            ) {}
        }
        if (result !is ScanUiResult.Idle) {
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
}
