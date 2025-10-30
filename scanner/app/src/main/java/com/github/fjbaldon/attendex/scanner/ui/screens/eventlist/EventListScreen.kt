package com.github.fjbaldon.attendex.scanner.ui.screens.eventlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel(),
    onEventSelected: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncResultState: State<String?> = viewModel.syncResult.collectAsState()
    val syncMessage = syncResultState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(syncMessage) {
        if (syncMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(syncMessage)
                viewModel.onSyncMessageShown()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.syncAttendance() }) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Sync, contentDescription = "Sync Data")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing || uiState.isLoading,
            onRefresh = { viewModel.refreshEvents() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading && !uiState.isRefreshing) {
                    CircularProgressIndicator()
                }
                else if (uiState.error != null) {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.refreshEvents() }
                    )
                }
                else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = if (uiState.events.isEmpty()) Arrangement.Center else Arrangement.spacedBy(
                            12.dp
                        )
                    ) {
                        if (uiState.events.isEmpty()) {
                            item { EmptyState() }
                        } else {
                            items(uiState.events, key = { it.id }) { event ->
                                EventCard(
                                    eventName = event.eventName,
                                    onClick = { onEventSelected(event.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(eventName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = eventName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "No active events found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Pull down to refresh",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
