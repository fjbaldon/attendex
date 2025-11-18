package com.github.fjbaldon.attendex.capture.feature.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.core.data.local.model.EventEntity
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
    val needsToSync: Boolean = false,
    val initialLoadFailed: Boolean = false
)

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
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
            } catch (_: Exception) {
                val errorMessage = "Failed to fetch events. Please check your network connection."
                _error.value = errorMessage
                if (isInitialLoad) {
                    _initialLoadFailed.value = true
                } else {
                    _syncResult.value = errorMessage
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

    fun syncAttendance() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = eventRepository.syncAttendanceRecords()
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
