package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
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

    if (uiState.isManualEntryOpen) {
        ManualEntryDialog(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            results = uiState.searchResults,
            onSelect = viewModel::onManualEntrySelected,
            onDismiss = { viewModel.toggleManualEntry(false) }
        )
    }

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
                    attendees = uiState.scannedAttendees,
                    hasFailedSyncs = uiState.hasFailedSyncs,
                    onRetry = viewModel::retryFailedScans
                )
            }
        },
        sheetPeekHeight = 100.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.eventName ?: "Scanner",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Total: ${uiState.globalScanCount} / ${uiState.totalRosterCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.isRosterSyncing) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Sync triggered by retry now */ }) {
                        if (uiState.hasFailedSyncs) {
                            Icon(Icons.Default.Warning, "Sync Failed", tint = MaterialTheme.colorScheme.error)
                        } else if (uiState.hasUnsyncedData) {
                            Icon(Icons.Default.CloudOff, "Unsynced Data", tint = Color(0xFFFF9800))
                        } else {
                            Icon(Icons.Default.CloudDone, "Synced", tint = Color(0xFF4CAF50))
                        }
                    }

                    IconButton(onClick = { viewModel.toggleManualEntry(true) }) {
                        Icon(Icons.Default.Keyboard, "Manual Entry")
                    }

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
                    scanMode = uiState.scanMode,
                    onTextFound = { text -> viewModel.processScannedText(text) },
                    torchEnabled = uiState.isTorchOn,
                    onTorchToggle = { hasFlash -> viewModel.onFlashUnitAvailabilityChange(hasFlash) }
                )

                ScannerOverlay(
                    result = uiState.lastScanResult,
                    isEventActive = uiState.isEventActive
                )

                ScanModeSelector(
                    currentMode = uiState.scanMode,
                    onModeSelected = { viewModel.toggleScanMode(it) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                )
            }
        }
    }
}

@Composable
fun ManualEntryDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AttendeeEntity>,
    onSelect: (AttendeeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Manual Entry",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Search Name or ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(results) { attendee ->
                        ListItem(
                            headlineContent = { Text("${attendee.firstName} ${attendee.lastName}") },
                            supportingContent = { Text(attendee.identity) },
                            modifier = Modifier.clickable { onSelect(attendee) }
                        )
                        HorizontalDivider()
                    }
                    if (results.isEmpty() && query.isNotEmpty()) {
                        item {
                            Text(
                                "No matches found",
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanModeSelector(
    currentMode: ScanMode,
    onModeSelected: (ScanMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeButton(
            text = "Text ID",
            icon = Icons.Default.TextFields,
            isSelected = currentMode == ScanMode.OCR,
            onClick = { onModeSelected(ScanMode.OCR) }
        )
        ModeButton(
            text = "QR Code",
            icon = Icons.Default.QrCodeScanner,
            isSelected = currentMode == ScanMode.QR,
            onClick = { onModeSelected(ScanMode.QR) }
        )
    }
}

@Composable
fun ModeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ScannedAttendeesSheetContent(
    attendees: List<ScannedItemUi>,
    hasFailedSyncs: Boolean,
    onRetry: () -> Unit
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

            if (hasFailedSyncs) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry Failed", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = attendees.size.toString(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
                items(attendees, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = {
                            Text(
                                item.name,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = { Text(item.identity) },
                        trailingContent = {
                            when {
                                item.isFailed -> Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = "Sync Failed",
                                    tint = MaterialTheme.colorScheme.error
                                )

                                item.isSynced -> Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = "Synced",
                                    tint = Color(0xFF4CAF50)
                                )

                                else -> Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = "Pending",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
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
    isEventActive: Boolean
) {
    val overlayState = when {
        !isEventActive -> OverlayState(
            text = "Event is not active",
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
                text = "Already Scanned",
                textColor = Color(0xFFFFC107),
                overlayColor = Color(0xFFFFC107).copy(alpha = 0.5f)
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
        if (overlayState.text.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 200.dp)
            ) {
                Text(
                    text = overlayState.text,
                    color = overlayState.textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.8f), shape = MaterialTheme.shapes.medium)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}
