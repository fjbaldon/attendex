package com.github.fjbaldon.attendex.capture.feature.scanner.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity

@Composable
fun ManualEntryDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AttendeeEntity>,
    onSelect: (AttendeeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        // === IMPROVEMENT: Use Surface for M3 Elevation & Colors ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(8.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Manual Entry",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Rounded Search Input
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Search Name or ID") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(results) { attendee ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    "${attendee.firstName} ${attendee.lastName}",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    attendee.identity,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent // Allow Surface color to show
                            ),
                            modifier = Modifier
                                .clickable { onSelect(attendee) }
                                .fillMaxWidth()
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    if (results.isEmpty() && query.isNotEmpty()) {
                        item {
                            Text(
                                "No matches found",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
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
