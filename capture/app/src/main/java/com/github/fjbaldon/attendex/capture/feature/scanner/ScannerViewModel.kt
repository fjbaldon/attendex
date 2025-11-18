package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.core.services.TtsService
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.data.event.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScanUiResult {
    data object Idle : ScanUiResult()
    data class Success(val attendeeDetails: String) : ScanUiResult()
    data class Error(val message: String) : ScanUiResult()
    data class AlreadyScanned(val identifier: String) : ScanUiResult()
    data object ScanningInactive : ScanUiResult()
}

data class ScannerUiState(
    val eventName: String? = null,
    val isLoading: Boolean = true,
    val lastScanResult: ScanUiResult = ScanUiResult.Idle,
    val scannedAttendees: List<AttendeeEntity> = emptyList(),
    val hasFlashUnit: Boolean = false,
    val isTorchOn: Boolean = false,
    val isEventActive: Boolean = true
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

    private val scannedAttendeesFlow: Flow<List<AttendeeEntity>> =
        eventRepository.getScannedAttendeesStream(eventId)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ScannerUiState> = combine(
        _isLoading,
        _lastScanResult,
        scannedAttendeesFlow,
        _torchState,
        _eventName,
        _isEventActive
    ) { flows: Array<Any?> ->
        ScannerUiState(
            isLoading = flows[0] as Boolean,
            lastScanResult = flows[1] as ScanUiResult,
            scannedAttendees = flows[2] as List<AttendeeEntity>,
            hasFlashUnit = (flows[3] as Pair<Boolean, Boolean>).first,
            isTorchOn = (flows[3] as Pair<Boolean, Boolean>).second,
            eventName = flows[4] as String?,
            isEventActive = flows[5] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState()
    )

    private var isProcessing = false

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

    fun processScannedText(scannedText: String) {
        if (isProcessing || !_isEventActive.value) return
        isProcessing = true

        viewModelScope.launch {
            when (val result = eventRepository.processScan(eventId, scannedText)) {
                is ScanResult.Success -> {
                    ttsService.speak(result.attendee.lastName)
                    _lastScanResult.value = ScanUiResult.Success(
                        attendeeDetails = "${result.attendee.firstName} ${result.attendee.lastName} (${result.attendee.uniqueIdentifier})"
                    )
                }

                is ScanResult.AttendeeNotFound -> {
                }

                is ScanResult.AlreadyScanned -> {
                    _lastScanResult.value = ScanUiResult.AlreadyScanned(scannedText)
                }
            }
            resetScanResultAfterDelay()
        }
    }

    private fun resetScanResultAfterDelay() {
        viewModelScope.launch {
            delay(250)
            _lastScanResult.value = ScanUiResult.Idle
            isProcessing = false
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
