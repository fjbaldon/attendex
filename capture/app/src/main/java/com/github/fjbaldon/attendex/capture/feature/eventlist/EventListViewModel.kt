package com.github.fjbaldon.attendex.capture.feature.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.core.data.local.model.EventEntity
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val events: List<EventEntity> = emptyList(),
    val error: String? = null,
    val unsyncedCount: Int = 0,
    val initialLoadFailed: Boolean = false
)

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository, // Now used in logout()
    syncManager: SyncManager
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
        eventRepository.getUnsyncedCountFlow(),
        _initialLoadFailed
    ) { flows: Array<Any?> ->
        EventListUiState(
            isLoading = flows[0] as Boolean,
            isRefreshing = flows[1] as Boolean,
            isSyncing = flows[2] as Boolean,
            error = flows[3] as String?,
            events = flows[4] as List<EventEntity>,
            unsyncedCount = flows[5] as Int,
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

        syncManager.startPeriodicSync()

        syncManager.triggerImmediateSync()
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
                eventRepository.refreshEvents().getOrThrow()
            } catch (_: Exception) { // FIX: Changed 'e' to '_' to suppress unused warning
                val cachedEvents = eventRepository.getEvents().first()

                if (cachedEvents.isNotEmpty()) {
                    _syncResult.value = "You are offline. Showing cached events."
                    _initialLoadFailed.value = false
                } else {
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

        // FIX: Call Repository DIRECTLY for manual sync.
        // This gives us immediate success/fail result for the UI.
        viewModelScope.launch {
            val result = eventRepository.syncEntries()

            _isSyncing.value = false

            result.onSuccess { count ->
                if (count > 0) {
                    _syncResult.value = "Successfully synced $count entries."
                } else {
                    // If count is 0 but banner is showing, it might mean
                    // the banner hasn't updated yet or data is in a weird state.
                    // Usually, this path won't happen if banner logic is correct.
                    _syncResult.value = "Data is up to date."
                }
            }

            result.onFailure { exception ->
                // Now the user knows WHY nothing happened
                _syncResult.value = "Sync Error: ${exception.message}"
            }
        }
    }

    // FIX: Added this function back to use authRepository and fix Screen error
    fun logout() {
        authRepository.logout()
    }
}
