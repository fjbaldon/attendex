package com.github.fjbaldon.attendex.capture.feature.eventlist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.fjbaldon.attendex.capture.core.data.local.model.EventEntity
import com.github.fjbaldon.attendex.capture.core.data.remote.SessionResponse
import com.github.fjbaldon.attendex.capture.feature.eventlist.components.SyncStatusBanner
import com.github.fjbaldon.attendex.capture.ui.common.SkeletonEventCard
import com.github.fjbaldon.attendex.capture.ui.common.TicketDivider
import com.github.fjbaldon.attendex.capture.ui.common.ticketShape
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel(),
    onEventSelected: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncMessage by viewModel.syncResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // FIX: Use MutableState object instead of 'by' delegate to silence "Assigned value never read"
    val showLogoutDialog = remember { mutableStateOf(false) }

    // Pull to Refresh Logic
    val isIndicatorVisible = remember { mutableStateOf(false) }
    val refreshStartTime = remember { mutableLongStateOf(0L) }
    val minDisplayTime = 500L

    LaunchedEffect(uiState.isRefreshing) {
        if (uiState.isRefreshing) {
            refreshStartTime.longValue = System.currentTimeMillis()
            isIndicatorVisible.value = true
        } else {
            val elapsedTime = System.currentTimeMillis() - refreshStartTime.longValue
            if (elapsedTime < minDisplayTime) {
                delay(minDisplayTime - elapsedTime)
            }
            isIndicatorVisible.value = false
        }
    }

    // Grouping Logic
    val groupedEvents = remember(uiState.events) {
        val now = Instant.now()
        val today = now.atZone(ZoneId.systemDefault()).toLocalDate()
        val sorted = uiState.events.sortedBy { it.startDate }

        sorted.groupBy { event ->
            try {
                val start = Instant.parse(event.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
                val end = Instant.parse(event.endDate).atZone(ZoneId.systemDefault()).toLocalDate()

                when {
                    !start.isAfter(today) && !end.isBefore(today) -> "Happening Now"
                    start.isAfter(today) -> "Upcoming"
                    else -> "Past Events"
                }
            } catch (_: Exception) {
                "Others"
            }
        }
    }

    val sortedGroups = remember(groupedEvents) {
        val order = listOf("Happening Now", "Upcoming", "Past Events", "Others")
        groupedEvents.toSortedMap(compareBy { order.indexOf(it) })
    }

    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out? An internet connection will be required to sign back in.") },
            confirmButton = {
                TextButton(onClick = {
                    // FIX: Close dialog state FIRST to ensure UI dismisses cleanly
                    showLogoutDialog.value = false
                    // Then trigger the logout logic
                    viewModel.logout()
                    onLogout()
                }) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }) { Text("Cancel") }
            }
        )
    }
    LaunchedEffect(syncMessage) {
        syncMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                actions = {
                    IconButton(onClick = { showLogoutDialog.value = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            SyncStatusBanner(
                unsyncedCount = uiState.unsyncedCount,
                isSyncing = uiState.isSyncing,
                onSyncClick = { viewModel.syncEntries() }
            )

            when {
                uiState.isLoading -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        repeat(4) { SkeletonEventCard() }
                    }
                }

                uiState.initialLoadFailed -> {
                    ErrorState(
                        message = uiState.error ?: "An unknown error occurred.",
                        onRetry = { viewModel.retryInitialLoad() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    PullToRefreshBox(
                        isRefreshing = isIndicatorVisible.value,
                        onRefresh = { viewModel.refreshEvents() },
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            verticalArrangement = if (uiState.events.isEmpty()) Arrangement.Center else Arrangement.Top
                        ) {
                            if (uiState.events.isEmpty()) {
                                item { EmptyState() }
                            } else {
                                sortedGroups.forEach { (header, events) ->
                                    stickyHeader {
                                        Surface(
                                            color = MaterialTheme.colorScheme.background,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = header,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                            )
                                        }
                                    }

                                    items(events, key = { it.id }) { event ->
                                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                            EventTicketCard(
                                                event = event,
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
        }
    }
}

@Composable
private fun EventTicketCard(
    event: EventEntity,
    onClick: () -> Unit
) {
    val status = remember(event.startDate, event.endDate) {
        getEventStatus(event.startDate, event.endDate)
    }
    val dateData = remember(event.startDate) { parseEventDate(event.startDate) }
    val sessionDisplay = remember(event.sessions) { formatSessionTimes(event.sessions) }

    val (statusText, statusColor, statusContainer) = when (status) {
        EventStatus.UPCOMING -> Triple(
            "Upcoming",
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
        EventStatus.PAST -> Triple(
            "Past",
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant
        )
        EventStatus.ACTIVE -> Triple(
            "Active",
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
        EventStatus.UNKNOWN -> Triple(
            "Unknown",
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.surface
        )
    }

    Surface(
        onClick = onClick,
        shape = ticketShape(24f, 20f),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp, // Material handles the shadow perfectly now
        modifier = Modifier
            .fillMaxWidth()
        // No need for manual .clip() or .background() or .shadow() modifiers
    ) {
        Column {
            // -- TOP SECTION (Info) --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Date Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = dateData.month,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateData.day,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sessionDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // -- DIVIDER --
            TicketDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // -- BOTTOM SECTION (Status & CTA) --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge
                Surface(
                    color = statusContainer,
                    contentColor = statusColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // CTA
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tap to Scan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .padding(start = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 32.dp)
            .padding(horizontal = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(100)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Active Events",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You're all caught up! Pull down to refresh if you're expecting a new schedule.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
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

// --- HELPER FUNCTIONS ---

private enum class EventStatus {
    UPCOMING, PAST, ACTIVE, UNKNOWN
}

data class DateDisplay(val month: String, val day: String)

private fun getEventStatus(start: String, end: String): EventStatus {
    val now = Instant.now()
    return try {
        val s = Instant.parse(start)
        val e = Instant.parse(end)

        when {
            now.isBefore(s) -> EventStatus.UPCOMING
            now.isAfter(e) -> EventStatus.PAST
            else -> EventStatus.ACTIVE
        }
    } catch (_: Exception) {
        EventStatus.UNKNOWN
    }
}

private fun parseEventDate(isoString: String): DateDisplay {
    return try {
        val date = Instant.parse(isoString).atZone(ZoneId.systemDefault())
        val month = DateTimeFormatter.ofPattern("MMM").withLocale(Locale.US).format(date).uppercase()
        val day = DateTimeFormatter.ofPattern("dd").withLocale(Locale.US).format(date)
        DateDisplay(month, day)
    } catch (_: Exception) {
        DateDisplay("---", "--")
    }
}

private fun formatSessionTimes(sessions: List<SessionResponse>): String {
    if (sessions.isEmpty()) return "No Sessions"

    return try {
        val fmt = DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.US)
        sessions
            .map { Instant.parse(it.targetTime).atZone(ZoneId.systemDefault()) }
            .sorted()
            .map { fmt.format(it) }
            .distinct()
            .joinToString(" • ")
    } catch (_: Exception) {
        "${sessions.size} Session(s)"
    }
}
