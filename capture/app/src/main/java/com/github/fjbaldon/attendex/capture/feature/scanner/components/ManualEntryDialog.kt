package com.github.fjbaldon.attendex.capture.feature.scanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity

@Composable
fun ManualEntryDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AttendeeEntity>,
    onSelect: (AttendeeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    // Use a full-screen dialog with transparent background to create a custom overlay feel
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)) // Dim background
                .clickable(onClick = onDismiss) // Click outside to close
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(enabled = false) {} // Prevent clicks passing through the card
            ) {
                // Search Bar Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Header / Input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )

                            TextField(
                                value = query,
                                onValueChange = onQueryChange,
                                placeholder = { Text("Type name or ID...") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                singleLine = true
                            )

                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, "Close")
                            }
                        }

                        HorizontalDivider()

                        // Results List (Limited height)
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            if (results.isEmpty() && query.isNotEmpty()) {
                                item {
                                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("No matching attendees found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else if (results.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Start typing to search...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            items(results) { attendee ->
                                ListItem(
                                    headlineContent = { Text("${attendee.firstName} ${attendee.lastName}") },
                                    supportingContent = { Text(attendee.identity, color = MaterialTheme.colorScheme.primary) },
                                    modifier = Modifier.clickable { onSelect(attendee) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Auto-focus the input
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
