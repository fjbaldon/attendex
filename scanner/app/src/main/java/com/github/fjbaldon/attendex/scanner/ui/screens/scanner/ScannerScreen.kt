package com.github.fjbaldon.attendex.scanner.ui.screens.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.scanner.ui.camera.CameraPreview

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
            ) {
                ScannedAttendeesSheetContent(
                    attendees = uiState.scannedAttendees
                )
            }
        },
        sheetPeekHeight = 64.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        topBar = {
            TopAppBar(
                title = { Text(uiState.eventName ?: "Scanner") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back"
                        )
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
                ScannerOverlay(result = uiState.lastScanResult)
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
            Text("Scanned Log", style = MaterialTheme.typography.titleMedium)
            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                Text(text = attendees.size.toString(), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        if (attendees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Scan an ID to begin.",
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
        headlineContent = {
            Text(
                "${attendee.lastName}, ${attendee.firstName}",
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = { Text(attendee.uniqueIdentifier) }
    )
}

private data class OverlayState(
    val text: String,
    val textColor: Color,
    val overlayColor: Color,
    val scanType: String? = null
)

@Composable
private fun ScannerOverlay(result: ScanUiResult) {
    val overlayState = when (result) {
        is ScanUiResult.Success -> OverlayState(
            text = result.attendeeDetails,
            textColor = Color(0xFF4CAF50),
            overlayColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
            scanType = result.type.replace("_", " ").replaceFirstChar { it.uppercase() }
        )

        is ScanUiResult.AlreadyScanned -> OverlayState(
            text = "Already Scanned: ${result.identifier}",
            textColor = Color(0xFFFFC107),
            overlayColor = Color(0xFFFFC107).copy(alpha = 0.5f)
        )

        is ScanUiResult.ScanningInactive -> OverlayState(
            text = "Scanning Inactive",
            textColor = Color(0xFFFFC107),
            overlayColor = Color.Transparent
        )

        is ScanUiResult.Idle, is ScanUiResult.Error -> OverlayState(
            text = "Point camera at an ID",
            textColor = Color.White,
            overlayColor = Color.Transparent
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayState.overlayColor)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
                .aspectRatio(4f)
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
                if (overlayState.scanType != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(overlayState.scanType, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = overlayState.text,
                    color = overlayState.textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
