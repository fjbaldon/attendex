package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.fjbaldon.attendex.capture.core.ui.camera.CameraOverlay
import com.github.fjbaldon.attendex.capture.core.ui.camera.CameraPreview
import com.github.fjbaldon.attendex.capture.feature.scanner.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val haptic = LocalHapticFeedback.current

    // Flash State
    var flashTrigger by remember { mutableStateOf(false) }

    // --- STATE MONITORING ---

    // 1. Monitor Sheet Visibility for Lazy Loading (DB Optimization)
    // If the sheet is Hidden, we tell VM to stop querying the DB.
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        val isVisible = scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden
        viewModel.onSheetStateChange(isVisible)
    }

    // 2. Monitor Sheet Expansion for Camera Pausing (Battery/CPU Optimization)
    // If sheet covers the screen (Expanded), stop camera analysis.
    val isSheetExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded ||
            scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

    // Handle Feedback (Haptic + Visual Flash)
    LaunchedEffect(uiState.lastScanResult) {
        when (uiState.lastScanResult) {
            is ScanUiResult.Success -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                flashTrigger = true
                delay(150)
                flashTrigger = false
            }
            is ScanUiResult.AlreadyScanned -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                flashTrigger = true
                delay(150)
                flashTrigger = false
            }
            is ScanUiResult.Error -> {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            else -> {}
        }
    }

    // Flash Animation
    val flashColor by animateColorAsState(
        targetValue = if (flashTrigger) Color.Green.copy(alpha = 0.3f) else Color.Transparent,
        animationSpec = tween(150),
        label = "flash_animation"
    )

    // Consolidated Scanning Enablement Logic
    val isScanningEnabled = uiState.isCameraEnabled &&
            !uiState.isManualEntryOpen &&
            uiState.lastScanResult is ScanUiResult.Idle &&
            !isSheetExpanded // <--- PAUSE CAMERA WHEN SHEET IS UP

    if (uiState.isManualEntryOpen) {
        ManualEntryDialog(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onManualEntryQueryChange,
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
                    totalScanCount = uiState.globalScanCount,
                    hasFailedSyncs = uiState.hasFailedSyncs,
                    searchQuery = uiState.recentScansQuery,
                    isFilteringUnsynced = uiState.isFilteringUnsynced,
                    onToggleUnsyncedFilter = viewModel::toggleUnsyncedFilter,
                    onSearchQueryChange = viewModel::onRecentScansQueryChange,
                    onRetry = viewModel::retryFailedScans,
                    onLoadAll = viewModel::loadFullHistory
                )
            }
        },
        sheetPeekHeight = 100.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        topBar = {
            ScannerTopBar(
                uiState = uiState,
                onNavigateBack = onNavigateBack,
                onRetrySync = viewModel::retryFailedScans,
                onManualEntryClick = { viewModel.toggleManualEntry(true) },
                onTorchToggle = { viewModel.onTorchToggle(!uiState.isTorchOn) }
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
            } else if (!uiState.isCameraEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Ready to Scan?",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.enableCamera() }) {
                            Text("Open Camera")
                        }
                    }
                }
            } else {
                CameraPreview(
                    scanMode = uiState.scanMode,
                    onTextFound = { text -> viewModel.processScannedText(text) },
                    torchEnabled = uiState.isTorchOn,
                    onTorchToggle = { hasFlash -> viewModel.onFlashUnitAvailabilityChange(hasFlash) },
                    isScanningEnabled = isScanningEnabled,
                    customRegex = uiState.identityRegex
                )

                CameraOverlay(
                    scanMode = uiState.scanMode,
                    scanResult = uiState.lastScanResult,
                    modifier = Modifier.fillMaxSize()
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
                        .padding(bottom = 140.dp) // INCREASED from 120.dp to 140.dp to fit the hint text
                )

                // Flash Overlay (Top Layer)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(flashColor)
                )
            }
        }
    }
}
