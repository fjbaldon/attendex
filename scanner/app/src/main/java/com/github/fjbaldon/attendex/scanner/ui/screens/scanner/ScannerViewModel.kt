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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()
    private var isProcessing = false

    init {
        loadEventDetails()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val eventName = eventRepository.getEventNameById(eventId)
            eventRepository.refreshAttendeesForEvent(eventId).onFailure {
                _uiState.update { s -> s.copy(lastScanResult = ScanUiResult.Error("Failed to load attendees.")) }
            }
            _uiState.update { it.copy(isLoading = false, eventName = eventName) }
        }
    }

    fun processScannedText(scannedText: String) {
        if (isProcessing) return

        if (_uiState.value.scannedAttendees.any { it.uniqueIdentifier == scannedText }) {
            val alreadyScannedAttendee =
                _uiState.value.scannedAttendees.first { it.uniqueIdentifier == scannedText }
            _uiState.update {
                it.copy(
                    lastScanResult = ScanUiResult.AlreadyScanned(
                        alreadyScannedAttendee.uniqueIdentifier
                    )
                )
            }
            resetScanResultAfterDelay()
            return
        }

        isProcessing = true
        viewModelScope.launch {
            val result = eventRepository.processScan(eventId, scannedText)

            when (result) {
                is ScanResult.Success -> {
                    ttsService.speak(result.attendee.lastName)
                    _uiState.update {
                        it.copy(
                            scannedAttendees = listOf(result.attendee) + it.scannedAttendees,
                            lastScanResult = ScanUiResult.Success(
                                "${result.attendee.firstName} ${result.attendee.lastName} (${result.attendee.uniqueIdentifier})"
                            )
                        )
                    }
                }

                is ScanResult.AttendeeNotFound -> { /* Do nothing */
                }

                is ScanResult.AlreadyScanned -> { /* This is handled by the local list check */
                }
            }

            resetScanResultAfterDelay()
        }
    }

    private fun resetScanResultAfterDelay() {
        viewModelScope.launch {
            delay(200)
            _uiState.update { it.copy(lastScanResult = ScanUiResult.Idle) }
            isProcessing = false
        }
    }

    fun onTorchToggle(isOn: Boolean) {
        _uiState.update { it.copy(isTorchOn = isOn) }
    }

    fun onFlashUnitAvailabilityChange(isAvailable: Boolean) {
        _uiState.update { it.copy(hasFlashUnit = isAvailable) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.shutdown()
    }
}
