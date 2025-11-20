package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.core.ui.camera.CameraPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                ScannedAttendeesSheetContent(
                    attendees = uiState.scannedAttendees
                )
            }
        },
        sheetPeekHeight = 80.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.eventName ?: "Scanner",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // Display the automatically selected session
                        Text(
                            text = uiState.selectedSession?.activityName ?: "No Active Session",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.hasFlashUnit) {
                        IconButton(onClick = { viewModel.onTorchToggle(!uiState.isTorchOn) }) {
                            Icon(
                                if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                "Toggle Flashlight"
                            )
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                CameraPreview(
                    onTextFound = { text -> viewModel.processScannedText(text) },
                    torchEnabled = uiState.isTorchOn,
                    onTorchToggle = { hasFlash -> viewModel.onFlashUnitAvailabilityChange(hasFlash) }
                )
                ScannerOverlay(
                    result = uiState.lastScanResult,
                    isEventActive = uiState.isEventActive,
                    hasSessionSelected = uiState.selectedSession != null
                )
            }
        }
    }
}

@Composable
private fun ScannedAttendeesSheetContent(
    attendees: List<AttendeeEntity>,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Scans", style = MaterialTheme.typography.titleMedium)
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
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No scans yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(attendees, key = { it.localId }) { attendee ->
                    ListItem(
                        headlineContent = {
                            Text(
                                "${attendee.lastName}, ${attendee.firstName}",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = { Text(attendee.identity) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private data class OverlayState(
    val text: String,
    val textColor: Color,
    val overlayColor: Color
)

@Composable
private fun ScannerOverlay(
    result: ScanUiResult,
    isEventActive: Boolean,
    hasSessionSelected: Boolean
) {
    val overlayState = when {
        !isEventActive -> OverlayState(
            text = "Event is not active",
            textColor = Color(0xFFFFC107),
            overlayColor = Color.Black.copy(alpha = 0.6f)
        )

        !hasSessionSelected -> OverlayState(
            text = "No active session found",
            textColor = Color(0xFFFFC107),
            overlayColor = Color.Black.copy(alpha = 0.6f)
        )

        else -> when (result) {
            is ScanUiResult.Success -> OverlayState(
                text = result.attendeeDetails,
                textColor = Color(0xFF4CAF50),
                overlayColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
            )

            is ScanUiResult.AlreadyScanned -> OverlayState(
                text = "Already Scanned: ${result.identifier}",
                textColor = Color(0xFFFFC107),
                overlayColor = Color(0xFFFFC107).copy(alpha = 0.5f)
            )

            is ScanUiResult.SessionNotSelected -> OverlayState(
                text = "No active session",
                textColor = Color(0xFFFFC107),
                overlayColor = Color.Black.copy(alpha = 0.5f)
            )

            is ScanUiResult.ScanningInactive -> OverlayState(
                text = "Processing...",
                textColor = Color.White,
                overlayColor = Color.Transparent
            )

            is ScanUiResult.Idle, is ScanUiResult.Error -> OverlayState(
                text = "",
                textColor = Color.White,
                overlayColor = Color.Transparent
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayState.overlayColor)
    ) {
        if (isEventActive && hasSessionSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.8f)
                    .aspectRatio(4f)
            ) {
                OutlinedCard(
                    border = BorderStroke(3.dp, Color.White.copy(alpha = 0.8f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {}
            }
        }

        if (overlayState.text.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 150.dp)
            ) {
                Text(
                    text = overlayState.text,
                    color = overlayState.textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
