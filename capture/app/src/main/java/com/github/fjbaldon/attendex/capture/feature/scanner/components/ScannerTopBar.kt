package com.github.fjbaldon.attendex.capture.feature.scanner.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.github.fjbaldon.attendex.capture.feature.scanner.ScannerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerTopBar(
    uiState: ScannerUiState,
    onNavigateBack: () -> Unit,
    onRetrySync: () -> Unit,
    onManualEntryClick: () -> Unit,
    onTorchToggle: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = uiState.eventName ?: "Scanner",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // FIX: Show explicit Roster Count so user knows they are offline-ready
                val subTitle = if (uiState.totalRosterCount > 0) {
                    "${uiState.totalRosterCount} in Roster"
                } else if (uiState.isRosterSyncing) {
                    "Downloading Roster..."
                } else {
                    "No Roster Loaded"
                }

                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.labelMedium,
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
            // Sync Status Icons - Themed
            IconButton(onClick = onRetrySync) {
                if (uiState.hasFailedSyncs) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Sync Failed",
                        tint = MaterialTheme.colorScheme.error // Red
                    )
                } else if (uiState.hasUnsyncedData) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Unsynced Data",
                        tint = MaterialTheme.colorScheme.errorContainer // Orange (Warning)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced",
                        tint = MaterialTheme.colorScheme.tertiary // Green (Success)
                    )
                }
            }

            IconButton(onClick = onManualEntryClick) {
                Icon(Icons.Default.Keyboard, "Manual Entry")
            }

            if (uiState.isCameraEnabled && uiState.hasFlashUnit) {
                IconButton(onClick = onTorchToggle) {
                    Icon(
                        if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        "Toggle Flashlight"
                    )
                }
            }
        }
    )
}
