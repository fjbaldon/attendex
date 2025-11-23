package com.github.fjbaldon.attendex.capture.feature.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.core.data.local.model.EventEntity
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val events: List<EventEntity> = emptyList(),
    val error: String? = null,
    val needsToSync: Boolean = false,
    val initialLoadFailed: Boolean = false
)

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    @param:ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    private val _isRefreshing = MutableStateFlow(false)
    private val _isSyncing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _initialLoadFailed = MutableStateFlow(false)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<EventListUiState> = combine(
        _isLoading,
        _isRefreshing,
        _isSyncing,
        _error,
        eventRepository.getEvents(),
        eventRepository.hasUnsyncedRecords,
        _initialLoadFailed
    ) { flows: Array<Any?> ->
        EventListUiState(
            isLoading = flows[0] as Boolean,
            isRefreshing = flows[1] as Boolean,
            isSyncing = flows[2] as Boolean,
            error = flows[3] as String?,
            events = flows[4] as List<EventEntity>,
            needsToSync = flows[5] as Boolean,
            initialLoadFailed = flows[6] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventListUiState()
    )

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult = _syncResult.asStateFlow()

    init {
        loadEvents(isInitialLoad = true)
        runHousekeeping()
    }

    private fun runHousekeeping() {
        viewModelScope.launch {
            eventRepository.runHousekeeping()
        }
    }

    fun retryInitialLoad() {
        loadEvents(isInitialLoad = true)
    }

    fun refreshEvents() {
        loadEvents(isInitialLoad = false)
    }

    private fun loadEvents(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) {
                _isLoading.value = true
                _initialLoadFailed.value = false
            } else {
                _isRefreshing.value = true
            }
            _error.value = null

            try {
                // Try to fetch from network
                eventRepository.refreshEvents().getOrThrow()
            } catch (_: Exception) {
                // FIX: Check if we have local data before declaring failure
                val cachedEvents = eventRepository.getEvents().first()

                if (cachedEvents.isNotEmpty()) {
                    // We are offline but have data. Show a warning, NOT a blocking error.
                    _syncResult.value = "You are offline. Showing cached events."
                    _initialLoadFailed.value = false
                } else {
                    // No local data AND no network. This is a true failure.
                    val errorMessage = "Failed to fetch events. Please check your network connection."
                    _error.value = errorMessage
                    if (isInitialLoad) {
                        _initialLoadFailed.value = true
                    } else {
                        _syncResult.value = errorMessage
                    }
                }
            } finally {
                if (isInitialLoad) {
                    _isLoading.value = false
                } else {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun syncEntries() {
        _isSyncing.value = true

        // FIXED: Use appScope instead of viewModelScope
        // This ensures sync finishes even if user navigates away
        appScope.launch {
            val result = eventRepository.syncEntries()

            // We must check if the ViewModel is still active before updating UI state
            // However, StateFlows are safe to update from background threads.
            result.onSuccess { count ->
                _syncResult.value = if (count > 0) {
                    val plural = if (count == 1) "record" else "records"
                    "$count $plural synced."
                } else {
                    "Data is already up to date."
                }
            }.onFailure {
                _syncResult.value = "Sync failed. Please try again."
            }
            _isSyncing.value = false
        }
    }

    fun onSyncMessageShown() {
        _syncResult.value = null
    }

    fun logout() {
        authRepository.logout()
    }
}
