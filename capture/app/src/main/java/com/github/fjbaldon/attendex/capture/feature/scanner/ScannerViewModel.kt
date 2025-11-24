package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.core.services.TtsService
import com.github.fjbaldon.attendex.capture.data.auth.SessionManager
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.data.event.ScanResult
import com.github.fjbaldon.attendex.capture.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val ttsService: TtsService,
    private val syncManager: SyncManager,
    sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val eventId: Long = savedStateHandle.get<Long>("eventId")!!
    private val _identityRegex = MutableStateFlow<String?>(null)

    // --- State Definitions ---
    private val _lastScanResult = MutableStateFlow<ScanUiResult>(ScanUiResult.Idle)
    private val _isCameraEnabled = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    private val _isRosterSyncing = MutableStateFlow(false)
    private val _torchState = MutableStateFlow(Pair(false, false))
    private val _eventName = MutableStateFlow<String?>(null)
    private val _isEventActive = MutableStateFlow(true)
    private val _scanMode = MutableStateFlow(ScanMode.OCR)

    private val _globalStats = MutableStateFlow(Pair(0L, 0L))

    private val _manualEntryQuery = MutableStateFlow("")
    private val _isManualEntryOpen = MutableStateFlow(false)
    private val _recentScansQuery = MutableStateFlow("")
    private val _isFilteringUnsynced = MutableStateFlow(false)
    private val _listLimit = MutableStateFlow(50)

    // --- Flows ---
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val baseScannedItemsFlow: Flow<List<ScannedItemUi>> = combine(
        _recentScansQuery.debounce(50),
        _listLimit
    ) { query, limit ->
        Pair(query, limit)
    }.flatMapLatest { (query, limit) ->
        if (query.isBlank()) {
            eventRepository.getScannedItemsStream(eventId, limit)
        } else {
            eventRepository.searchScannedItems(eventId, query)
        }
    }

    private val filteredRecentScans: Flow<List<ScannedItemUi>> = combine(
        baseScannedItemsFlow,
        _isFilteringUnsynced
    ) { items, filterUnsynced ->
        if (filterUnsynced) items.filter { !it.isSynced } else items
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val _rosterSearchResults = _manualEntryQuery
        .debounce(50)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else eventRepository.searchAttendees(eventId, query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ScannerUiState> = combine(
        _isLoading,
        _lastScanResult,
        filteredRecentScans,
        _torchState,
        _eventName,
        _isEventActive,
        _scanMode,
        eventRepository.hasUnsyncedRecords,
        _rosterSearchResults,
        _manualEntryQuery,
        _isManualEntryOpen,
        _isRosterSyncing,
        _globalStats,
        _isCameraEnabled,
        _recentScansQuery,
        _isFilteringUnsynced,
        _identityRegex
    ) { flows ->
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
            hasFailedSyncs = hasFailed,
            isCameraEnabled = flows[13] as Boolean,
            recentScansQuery = flows[14] as String,
            isFilteringUnsynced = flows[15] as Boolean,
            identityRegex = flows[16] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState()
    )

    private val isProcessing = AtomicBoolean(false)
    private var resetJob: Job? = null
    private var errorDebounceJob: Job? = null // NEW: To hold back error messages
    private var lastSpokenText: String? = null
    private var lastSpokenTime: Long = 0

    init {
        loadEventDetails()
        loadLocalStats()
        refreshRoster()
        _identityRegex.value = sessionManager.identityRegex
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

    private fun loadLocalStats() {
        viewModelScope.launch {
            val stats = eventRepository.getLocalEventStats(eventId)
            _globalStats.value = stats
        }
    }

    private fun refreshRoster() {
        viewModelScope.launch {
            _isRosterSyncing.value = true
            eventRepository.syncRosterForEvent(eventId)
            loadLocalStats()
            _isRosterSyncing.value = false
        }
    }

    fun enableCamera() { _isCameraEnabled.value = true }
    fun toggleScanMode(mode: ScanMode) { _scanMode.value = mode }
    fun onManualEntryQueryChange(query: String) { _manualEntryQuery.value = query }
    fun onRecentScansQueryChange(query: String) { _recentScansQuery.value = query }
    fun toggleManualEntry(isOpen: Boolean) {
        _isManualEntryOpen.value = isOpen
        if (!isOpen) _manualEntryQuery.value = ""
    }
    fun toggleUnsyncedFilter() { _isFilteringUnsynced.update { !it } }
    fun loadFullHistory() { _listLimit.value = 100000 }
    fun resetListLimit() { _listLimit.value = 50 }
    fun onManualEntrySelected(attendee: AttendeeEntity) {
        processScannedText(attendee.identity)
        toggleManualEntry(false)
    }
    fun retryFailedScans() { viewModelScope.launch { eventRepository.retryFailedEntries(eventId) } }
    fun onTorchToggle(isOn: Boolean) { _torchState.update { Pair(it.first, isOn) } }
    fun onFlashUnitAvailabilityChange(isAvailable: Boolean) { _torchState.update { Pair(isAvailable, it.second) } }

    private fun speakWithDebounce(text: String) {
        val now = System.currentTimeMillis()
        if (text == lastSpokenText && now - lastSpokenTime < 3000) return

        ttsService.speak(text)
        lastSpokenText = text
        lastSpokenTime = now
    }

    fun processScannedText(scannedText: String) {
        if (!_isEventActive.value || !isProcessing.compareAndSet(false, true)) return

        viewModelScope.launch {
            try {
                when (val result = eventRepository.processScan(eventId, scannedText)) {
                    is ScanResult.Success -> {
                        // Cancel pending error/reset jobs immediately
                        errorDebounceJob?.cancel()
                        resetJob?.cancel()

                        _lastScanResult.value = ScanUiResult.Success(
                            attendeeDetails = "${result.attendee.firstName} ${result.attendee.lastName}"
                        )
                        val current = _globalStats.value
                        _globalStats.value = Pair(current.first + 1, current.second)
                        speakWithDebounce(result.attendee.firstName)
                        syncManager.triggerImmediateSync()

                        // CHANGED: 1500ms -> 800ms (Faster reset for next scan)
                        scheduleReset(800)
                    }
                    is ScanResult.AlreadyScanned -> {
                        errorDebounceJob?.cancel()
                        resetJob?.cancel()

                        val details = "${result.attendee.firstName} ${result.attendee.lastName}"
                        val newState = ScanUiResult.AlreadyScanned(details)

                        // Always update to trigger UI flash even if same state
                        _lastScanResult.value = newState
                        speakWithDebounce(result.attendee.firstName)

                        scheduleReset(800)
                    }
                    is ScanResult.AttendeeNotFound -> {
                        // Only show error if we aren't currently showing a Success/AlreadyScanned
                        if (_lastScanResult.value !is ScanUiResult.Success && _lastScanResult.value !is ScanUiResult.AlreadyScanned) {

                            if (errorDebounceJob?.isActive != true) {
                                errorDebounceJob = launch {
                                    // CHANGED: 250ms -> 50ms (Minimal debounce for flicker)
                                    delay(50)
                                    _lastScanResult.value = ScanUiResult.NotFound(scannedText)

                                    // CHANGED: 1000ms -> 500ms (Quick recovery from error)
                                    scheduleReset(500)
                                }
                            }
                        }
                    }
                }
            } finally {
                isProcessing.set(false)
            }
        }
    }

    private fun scheduleReset(delayMs: Long) {
        resetJob = viewModelScope.launch {
            delay(delayMs)
            _lastScanResult.value = ScanUiResult.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.shutdown()
    }
}
