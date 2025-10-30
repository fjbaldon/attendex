package com.github.fjbaldon.attendex.scanner.ui.screens.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.scanner.data.local.model.EventEntity
import com.github.fjbaldon.attendex.scanner.data.repository.AuthRepository
import com.github.fjbaldon.attendex.scanner.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val events: List<EventEntity> = emptyList(),
    val error: String? = null,
    val needsToSync: Boolean = false
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

    val uiState: StateFlow<EventListUiState> = run {
        val flows = listOf(
            _isLoading,
            _isRefreshing,
            _isSyncing,
            _error,
            eventRepository.getEvents(),
            eventRepository.hasUnsyncedRecords
        )

        combine(flows) { values ->
            val isLoading = values[0] as Boolean
            val isRefreshing = values[1] as Boolean
            val isSyncing = values[2] as Boolean
            val error = values[3] as String?
            @Suppress("UNCHECKED_CAST")
            val events = values[4] as List<EventEntity>
            val needsToSync = values[5] as Boolean

            EventListUiState(isLoading, isRefreshing, isSyncing, events, error, needsToSync)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EventListUiState()
        )
    }

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult = _syncResult.asStateFlow()

    init {
        loadEvents(isInitialLoad = true)
    }

    fun refreshEvents() {
        loadEvents(isInitialLoad = false)
    }

    private fun loadEvents(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) _isLoading.value = true else _isRefreshing.value = true
            _error.value = null
            eventRepository.refreshEvents().onFailure {
                _error.value = "Failed to fetch events. Check connection."
            }
            if (isInitialLoad) _isLoading.value = false else _isRefreshing.value = false
        }
    }

    fun syncAttendance() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = eventRepository.syncAttendanceRecords()
            result.onSuccess { count ->
                _syncResult.value = if (count > 0) "$count records synced successfully." else "Already up to date."
            }.onFailure {
                _syncResult.value = "Sync failed: ${it.message}"
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
