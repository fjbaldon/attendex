package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.fjbaldon.attendex.capture.core.ui.camera.CameraOverlay
import com.github.fjbaldon.attendex.capture.core.ui.camera.CameraPreview
import com.github.fjbaldon.attendex.capture.feature.scanner.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val haptic = LocalHapticFeedback.current

    // FIX: Watch for bottom sheet collapse to reset list limit
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
            viewModel.resetListLimit()
        }
    }

    LaunchedEffect(uiState.lastScanResult) {
        when (uiState.lastScanResult) {
            is ScanUiResult.Success -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            is ScanUiResult.Error -> {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            else -> {}
        }
    }

    val isScanningEnabled = uiState.isCameraEnabled &&
            !uiState.isManualEntryOpen &&
            uiState.lastScanResult is ScanUiResult.Idle

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
                        .padding(bottom = 120.dp)
                )
            }
        }
    }
}
