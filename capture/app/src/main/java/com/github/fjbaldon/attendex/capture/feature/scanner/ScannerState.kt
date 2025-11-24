package com.github.fjbaldon.attendex.capture.feature.scanner

import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity

enum class ScanMode {
    OCR, QR
}

sealed class ScanUiResult {
    data object Idle : ScanUiResult()
    data class Success(val attendeeDetails: String) : ScanUiResult()
    data class Error(val message: String) : ScanUiResult()
    data class AlreadyScanned(val attendeeDetails: String) : ScanUiResult()
    data class NotFound(val scannedValue: String) : ScanUiResult()
}

data class ScannedItemUi(
    val id: Long,
    val name: String,
    val identity: String,
    val isSynced: Boolean,
    val isFailed: Boolean
)

data class ScannerUiState(
    val eventName: String? = null,
    val isCameraEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val isRosterSyncing: Boolean = false,
    val lastScanResult: ScanUiResult = ScanUiResult.Idle,
    val scannedAttendees: List<ScannedItemUi> = emptyList(), // Filtered list
    val hasFlashUnit: Boolean = false,
    val isTorchOn: Boolean = false,
    val isEventActive: Boolean = true,
    val scanMode: ScanMode = ScanMode.OCR,
    val hasUnsyncedData: Boolean = false,
    val searchResults: List<AttendeeEntity> = emptyList(),
    val searchQuery: String = "", // Roster Search (Manual Entry)
    val recentScansQuery: String = "", // New: Recent Scans Search
    val isFilteringUnsynced: Boolean = false, // NEW: Filter Togglec
    val isManualEntryOpen: Boolean = false,
    val globalScanCount: Long = 0,
    val totalRosterCount: Long = 0,
    val hasFailedSyncs: Boolean = false,
    val identityRegex: String? = null
)
