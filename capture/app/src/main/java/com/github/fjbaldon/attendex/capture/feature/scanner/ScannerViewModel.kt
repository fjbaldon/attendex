package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.core.services.TtsService
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.data.event.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // Simplified: Just success. No "Late/Early" warning.
    data class Success(val attendeeDetails: String) : ScanUiResult()
    data class Error(val message: String) : ScanUiResult()
    data class AlreadyScanned(val identifier: String) : ScanUiResult()
}

data class ScannedItemUi(
    val id: Long, // Entry ID
    val name: String,
    val identity: String,
    val isSynced: Boolean,
    val isFailed: Boolean
)

data class ScannerUiState(
    val eventName: String? = null,
    val isLoading: Boolean = true,
    val lastScanResult: ScanUiResult = ScanUiResult.Idle,
    val scannedAttendees: List<ScannedItemUi> = emptyList(),
    val hasFlashUnit: Boolean = false,
    val isTorchOn: Boolean = false,
    val isEventActive: Boolean = true,
    val scanMode: ScanMode = ScanMode.OCR
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
    private val _torchState = MutableStateFlow(Pair(false, false))
    private val _eventName = MutableStateFlow<String?>(null)
    private val _isEventActive = MutableStateFlow(true)
    private val _scanMode = MutableStateFlow(ScanMode.OCR)

    private val scannedItemsFlow: Flow<List<ScannedItemUi>> =
        eventRepository.getScannedItemsStream(eventId)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ScannerUiState> = combine(
        _isLoading,
        _lastScanResult,
        scannedItemsFlow,
        _torchState,
        _eventName,
        _isEventActive,
        _scanMode
    ) { flows: Array<Any?> ->
        ScannerUiState(
            isLoading = flows[0] as Boolean,
            lastScanResult = flows[1] as ScanUiResult,
            scannedAttendees = flows[2] as List<ScannedItemUi>,
            hasFlashUnit = (flows[3] as Pair<Boolean, Boolean>).first,
            isTorchOn = (flows[3] as Pair<Boolean, Boolean>).second,
            eventName = flows[4] as String?,
            isEventActive = flows[5] as Boolean,
            scanMode = flows[6] as ScanMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState()
    )

    private val isProcessing = AtomicBoolean(false)

    init {
        loadEventDetails()
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

    fun toggleScanMode(mode: ScanMode) {
        _scanMode.value = mode
    }

    fun processScannedText(scannedText: String) {
        if (!_isEventActive.value || !isProcessing.compareAndSet(false, true)) return

        viewModelScope.launch {
            // THIN CLIENT: We pass only EventID and Identity. No Session ID.
            when (val result = eventRepository.processScan(eventId, scannedText)) {
                is ScanResult.Success -> {
                    // FEEDBACK: Speak First Name Only (Positive Reinforcement)
                    ttsService.speak(result.attendee.firstName)

                    _lastScanResult.value = ScanUiResult.Success(
                        attendeeDetails = "${result.attendee.firstName} ${result.attendee.lastName}"
                    )
                }

                is ScanResult.AlreadyScanned -> {
                    // Distinct sound or phrase for duplicate
                    ttsService.speak("Already Scanned")
                    _lastScanResult.value = ScanUiResult.AlreadyScanned(scannedText)
                }

                is ScanResult.AttendeeNotFound -> {
                    // Optional: Handle Not Found
                }
            }
            resetScanResultAfterDelay()
        }
    }

    private fun resetScanResultAfterDelay() {
        viewModelScope.launch {
            // CHANGED: 750 -> 150 for high-speed throughput
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
