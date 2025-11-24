package com.github.fjbaldon.attendex.capture.feature.scanner.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.fjbaldon.attendex.capture.feature.scanner.ScannedItemUi

@Composable
fun ScannedAttendeesSheetContent(
    attendees: List<ScannedItemUi>,
    totalScanCount: Long,
    hasFailedSyncs: Boolean,
    searchQuery: String,
    isFilteringUnsynced: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleUnsyncedFilter: () -> Unit,
    onRetry: () -> Unit,
    onLoadAll: () -> Unit
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry Failed", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    val displayCount = if (searchQuery.isNotEmpty() || isFilteringUnsynced) {
                        attendees.size.toLong()
                    } else {
                        totalScanCount
                    }
                    Text(
                        text = displayCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )

            IconToggleButton(
                checked = isFilteringUnsynced,
                onCheckedChange = { onToggleUnsyncedFilter() },
                colors = IconButtonDefaults.iconToggleButtonColors(
                    checkedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    checkedContentColor = MaterialTheme.colorScheme.errorContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                if (isFilteringUnsynced) {
                    Icon(Icons.Default.FilterListOff, contentDescription = "Show All")
                } else {
                    Icon(Icons.Default.FilterList, contentDescription = "Show Unsynced Only")
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
                    if (isFilteringUnsynced) "All scans are synced." else "No scans found.",
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
                                    tint = MaterialTheme.colorScheme.tertiary
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

                // FIX: "Load All" Button if data is hidden
                if (attendees.size < totalScanCount && searchQuery.isEmpty() && !isFilteringUnsynced) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = onLoadAll) {
                                Text("Load older scans (${totalScanCount - attendees.size} hidden)")
                            }
                        }
                    }
                }
            }
        }
    }
}
