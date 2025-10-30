package com.github.fjbaldon.attendex.scanner.ui.screens.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.scanner.data.repository.EventRepository
import com.github.fjbaldon.attendex.scanner.data.repository.ScanResult
import com.github.fjbaldon.attendex.scanner.di.TtsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScanUiResult {
    data object Idle : ScanUiResult()
    data class Success(val attendeeDetails: String, val type: String) : ScanUiResult()
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

    private val scannedAttendeesFlow: Flow<List<AttendeeEntity>> =
        eventRepository.getScannedAttendeesStream(eventId, "CHECK_IN")

    val uiState: StateFlow<ScannerUiState> = combine(
        _isLoading,
        _lastScanResult,
        scannedAttendeesFlow,
        _torchState,
        _eventName
    ) { isLoading, lastScanResult, scannedAttendees, torchState, eventName ->
        ScannerUiState(
            isLoading = isLoading,
            lastScanResult = lastScanResult,
            scannedAttendees = scannedAttendees,
            hasFlashUnit = torchState.first,
            isTorchOn = torchState.second,
            eventName = eventName
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
            _isLoading.value = false
        }
    }

    fun processScannedText(scannedText: String) {
        if (isProcessing) return
        isProcessing = true

        viewModelScope.launch {
            val scanType = eventRepository.determineScanType(eventId)
            if (scanType == null) {
                _lastScanResult.value = ScanUiResult.ScanningInactive
                resetScanResultAfterDelay()
                return@launch
            }

            if (uiState.value.scannedAttendees.any { it.uniqueIdentifier == scannedText }) {
                _lastScanResult.value = ScanUiResult.AlreadyScanned(scannedText)
                resetScanResultAfterDelay()
                return@launch
            }

            when (val result = eventRepository.processScan(eventId, scannedText, scanType)) {
                is ScanResult.Success -> {
                    ttsService.speak(result.attendee.lastName)
                    _lastScanResult.value = ScanUiResult.Success(
                        attendeeDetails = "${result.attendee.firstName} ${result.attendee.lastName} (${result.attendee.uniqueIdentifier})",
                        type = scanType
                    )
                }

                is ScanResult.AttendeeNotFound -> { /* Do nothing for silent failure */
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
            delay(1200)
            _lastScanResult.value = ScanUiResult.Idle
            isProcessing = false
        }
    }

    fun onTorchToggle(isOn: Boolean) {
        _torchState.update { it.copy(second = isOn) }
    }

    fun onFlashUnitAvailabilityChange(isAvailable: Boolean) {
        _torchState.update { it.copy(first = isAvailable) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.shutdown()
    }
}
