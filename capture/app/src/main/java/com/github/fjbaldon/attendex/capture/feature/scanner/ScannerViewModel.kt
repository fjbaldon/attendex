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
import kotlin.math.max

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
    private val _isHistoryVisible = MutableStateFlow(false)

    // --- Optimized Cooldown Logic ---
    private val scanCoolDowns = mutableMapOf<String, Long>()
    // Reduced to 2 seconds. Enough to stop jitter, fast enough for retry.
    private val cooldownMs = 2000L
    private val minDisplayMs = 1500L

    private val isProcessing = AtomicBoolean(false)
    private var resetJob: Job? = null
    private var errorDebounceJob: Job? = null

    // --- Flows ---
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val baseScannedItemsFlow: Flow<List<ScannedItemUi>> = combine(
        _recentScansQuery.debounce(50),
        _listLimit,
        _isHistoryVisible
    ) { query, limit, isVisible ->
        Triple(query, limit, isVisible)
    }.flatMapLatest { (query, limit, isVisible) ->
        if (!isVisible) {
            flowOf(emptyList())
        } else {
            if (query.isBlank()) {
                eventRepository.getScannedItemsStream(eventId, limit)
            } else {
                eventRepository.searchScannedItems(eventId, query)
            }
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
        ScannerUiState(
            isLoading = flows[0] as Boolean,
            lastScanResult = flows[1] as ScanUiResult,
            scannedAttendees = flows[2] as List<ScannedItemUi>,
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
            globalScanCount = (flows[12] as Pair<Long, Long>).first,
            totalRosterCount = (flows[12] as Pair<Long, Long>).second,
            hasFailedSyncs = (flows[2] as List<ScannedItemUi>).any { it.isFailed },
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

    // Actions
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

    fun onManualEntrySelected(attendee: AttendeeEntity) {
        processScannedText(attendee.identity)
        toggleManualEntry(false)
    }

    fun retryFailedScans() { viewModelScope.launch { eventRepository.retryFailedEntries(eventId) } }
    fun onTorchToggle(isOn: Boolean) { _torchState.update { Pair(it.first, isOn) } }
    fun onFlashUnitAvailabilityChange(isAvailable: Boolean) { _torchState.update { Pair(isAvailable, it.second) } }

    fun onSheetStateChange(isOpen: Boolean) {
        if (_isHistoryVisible.value != isOpen) {
            _isHistoryVisible.value = isOpen
            if (!isOpen) {
                _listLimit.value = 50
                _recentScansQuery.value = ""
            }
        }
    }

    // --- CORE LOGIC ---
    fun processScannedText(scannedText: String) {
        val now = System.currentTimeMillis()

        // 1. Cool Down Check (Still prevents Frame-by-Frame spam of the SAME card)
        if (scanCoolDowns.containsKey(scannedText)) {
            val lastScan = scanCoolDowns[scannedText]!!
            if (now - lastScan < cooldownMs) return
        }

        if (!_isEventActive.value || !isProcessing.compareAndSet(false, true)) return

        viewModelScope.launch {
            try {
                if (scanCoolDowns.size > 100) {
                    // Remove entries older than the cooldown period
                    scanCoolDowns.entries.removeIf { now - it.value > cooldownMs }
                }

                scanCoolDowns[scannedText] = now

                // 2. Interrupt any previous UI tasks
                resetJob?.cancel()
                errorDebounceJob?.cancel()

                when (val result = eventRepository.processScan(eventId, scannedText)) {
                    is ScanResult.Success -> {
                        // 3. SPEED HACK: Valid Scan!
                        // If we successfully scanned a new person, clear the cooldowns for everyone else.
                        // This allows rapid A -> B -> A switching without waiting 2 seconds.
                        scanCoolDowns.clear()
                        scanCoolDowns[scannedText] = now // Re-add current one so we don't spam IT

                        _lastScanResult.value = ScanUiResult.Success(
                            attendeeDetails = "${result.attendee.firstName} ${result.attendee.lastName}"
                        )
                        val current = _globalStats.value
                        _globalStats.value = Pair(current.first + 1, current.second)

                        syncManager.triggerImmediateSync()

                        // Speak + Visual Sync (With Safety Buffer)
                        speakAndClearUi(result.attendee.firstName)
                    }
                    is ScanResult.AlreadyScanned -> {
                        val details = "${result.attendee.firstName} ${result.attendee.lastName}"
                        _lastScanResult.value = ScanUiResult.AlreadyScanned(details)

                        // Silent Visual Confirmation (Visual only, no TTS)
                        scheduleReset(500)
                    }
                    is ScanResult.AttendeeNotFound -> {
                        if (_lastScanResult.value !is ScanUiResult.Success) {
                            _lastScanResult.value = ScanUiResult.NotFound(scannedText)
                            scheduleReset(500)
                        }
                    }
                }
            } finally {
                isProcessing.set(false)
            }
        }
    }

    private fun speakAndClearUi(text: String) {
        val startTime = System.currentTimeMillis()

        // TTS Audio
        ttsService.speak(text) {
            // When TTS finishes...
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = max(0L, minDisplayMs - elapsedTime)

            resetJob = viewModelScope.launch {
                // Ensure green box stays for at least 1.5s total (unless interrupted)
                if (remainingTime > 0) {
                    delay(remainingTime)
                }
                _lastScanResult.value = ScanUiResult.Idle
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
