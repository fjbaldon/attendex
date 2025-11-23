package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.core.services.TtsService
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.data.event.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

enum class ScanMode {
    OCR, QR
}

sealed class ScanUiResult {
    data object Idle : ScanUiResult()
    data class Success(val attendeeDetails: String) : ScanUiResult()
    data class Error(val message: String) : ScanUiResult()
    data class AlreadyScanned(val identifier: String) : ScanUiResult()
}

data class ScannedItemUi(
    val id: Long,
    val name: String,
    val identity: String,
    val isSynced: Boolean,
    val isFailed: Boolean
)

data class ScannerUiState(
    val eventName: String? = null,
    val isLoading: Boolean = true,
    val isRosterSyncing: Boolean = false,
    val lastScanResult: ScanUiResult = ScanUiResult.Idle,
    val scannedAttendees: List<ScannedItemUi> = emptyList(),
    val hasFlashUnit: Boolean = false,
    val isTorchOn: Boolean = false,
    val isEventActive: Boolean = true,
    val scanMode: ScanMode = ScanMode.OCR,
    val hasUnsyncedData: Boolean = false,
    val searchResults: List<AttendeeEntity> = emptyList(),
    val searchQuery: String = "",
    val isManualEntryOpen: Boolean = false,
    val globalScanCount: Long = 0,
    val totalRosterCount: Long = 0,
    val hasFailedSyncs: Boolean = false
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val ttsService: TtsService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val eventId: Long = savedStateHandle.get<Long>("eventId")!!

    private val _lastScanResult = MutableStateFlow<ScanUiResult>(ScanUiResult.Idle)
    private val _isLoading = MutableStateFlow(true)
    private val _isRosterSyncing = MutableStateFlow(false)
    private val _torchState = MutableStateFlow(Pair(false, false))
    private val _eventName = MutableStateFlow<String?>(null)
    private val _isEventActive = MutableStateFlow(true)
    private val _scanMode = MutableStateFlow(ScanMode.OCR)

    private val _searchQuery = MutableStateFlow("")
    private val _isManualEntryOpen = MutableStateFlow(false)

    private val _globalStats = MutableStateFlow(Pair(0L, 0L))

    private val scannedItemsFlow: Flow<List<ScannedItemUi>> =
        eventRepository.getScannedItemsStream(eventId)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val _searchResults = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else eventRepository.searchAttendees(eventId, query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ScannerUiState> = combine(
        _isLoading,
        _lastScanResult,
        scannedItemsFlow,
        _torchState,
        _eventName,
        _isEventActive,
        _scanMode,
        eventRepository.hasUnsyncedRecords,
        _searchResults,
        _searchQuery,
        _isManualEntryOpen,
        _isRosterSyncing,
        _globalStats
    ) { flows: Array<Any?> ->
        val scannedItems = flows[2] as List<ScannedItemUi>
        val hasFailed = scannedItems.any { it.isFailed }
        val stats = flows[12] as Pair<Long, Long>

        ScannerUiState(
            isLoading = flows[0] as Boolean,
            lastScanResult = flows[1] as ScanUiResult,
            scannedAttendees = scannedItems,
            hasFlashUnit = (flows[3] as Pair<Boolean, Boolean>).first,
            isTorchOn = (flows[3] as Pair<Boolean, Boolean>).second,
            eventName = flows[4] as String?,
            isEventActive = flows[5] as Boolean,
            scanMode = flows[6] as ScanMode,
            hasUnsyncedData = flows[7] as Boolean,
            searchResults = flows[8] as List<AttendeeEntity>,
            searchQuery = flows[9] as String,
            isManualEntryOpen = flows[10] as Boolean,
            isRosterSyncing = flows[11] as Boolean,
            globalScanCount = stats.first,
            totalRosterCount = stats.second,
            hasFailedSyncs = hasFailed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState()
    )

    private val isProcessing = AtomicBoolean(false)

    init {
        loadEventDetails()
        refreshRoster()
        pollStats()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _eventName.value = eventRepository.getEventNameById(eventId)
            _isEventActive.value = eventRepository.isEventActive(eventId)
            eventRepository.primeLastScannedIdentifier(eventId)
            _isLoading.value = false
        }
    }

    private fun refreshRoster() {
        viewModelScope.launch {
            _isRosterSyncing.value = true
            eventRepository.syncRosterForEvent(eventId)
            _isRosterSyncing.value = false
        }
    }

    private fun pollStats() {
        viewModelScope.launch {
            while(true) {
                val result = eventRepository.fetchEventStats(eventId)
                result.onSuccess {
                    _globalStats.value = Pair(it.totalScans, it.totalRoster)
                }
                delay(30000)
            }
        }
    }

    fun retryFailedScans() {
        viewModelScope.launch {
            eventRepository.retryFailedEntries(eventId)
        }
    }

    fun toggleScanMode(mode: ScanMode) {
        _scanMode.value = mode
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleManualEntry(isOpen: Boolean) {
        _isManualEntryOpen.value = isOpen
        if (!isOpen) _searchQuery.value = ""
    }

    fun onManualEntrySelected(attendee: AttendeeEntity) {
        processScannedText(attendee.identity)
        toggleManualEntry(false)
    }

    fun processScannedText(scannedText: String) {
        if (!_isEventActive.value || !isProcessing.compareAndSet(false, true)) return

        viewModelScope.launch {
            when (val result = eventRepository.processScan(eventId, scannedText)) {
                is ScanResult.Success -> {
                    ttsService.speak(result.attendee.firstName)
                    _lastScanResult.value = ScanUiResult.Success(
                        attendeeDetails = "${result.attendee.firstName} ${result.attendee.lastName}"
                    )
                    val current = _globalStats.value
                    _globalStats.value = Pair(current.first + 1, current.second)
                }
                is ScanResult.AlreadyScanned -> {
                    ttsService.speak("Already Scanned")
                    _lastScanResult.value = ScanUiResult.AlreadyScanned(scannedText)
                }
                is ScanResult.AttendeeNotFound -> {
                }
            }
            resetScanResultAfterDelay()
        }
    }

    private fun resetScanResultAfterDelay() {
        viewModelScope.launch {
            delay(150)
            _lastScanResult.value = ScanUiResult.Idle
            isProcessing.set(false)
        }
    }

    fun onTorchToggle(isOn: Boolean) {
        _torchState.update { Pair(it.first, isOn) }
    }

    fun onFlashUnitAvailabilityChange(isAvailable: Boolean) {
        _torchState.update { Pair(isAvailable, it.second) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.shutdown()
    }
}
