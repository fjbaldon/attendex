package com.github.fjbaldon.attendex.scanner.ui.screens.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.scanner.data.repository.EventRepository
import com.github.fjbaldon.attendex.scanner.data.repository.ScanResult
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
    val hasFlashUnit: Boolean = false,
    val isTorchOn: Boolean = false,
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId")!!
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()
    private var isProcessing = false
    private var lastScannedQrCode: String? = null
    private var lastScannedIdentifier: String? = null

    init {
        refreshAttendees()
    }

    private fun refreshAttendees() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            eventRepository.refreshAttendeesForEvent(eventId).onFailure {
                _uiState.update { s -> s.copy(lastScanResult = ScanUiResult.Error("Failed to load attendees. Please check connection.")) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun processQrCode(qrCodeHash: String) {
        if (isProcessing) return

        if (qrCodeHash == lastScannedQrCode) {
            _uiState.update {
                it.copy(
                    lastScanResult = ScanUiResult.AlreadyScanned(
                        lastScannedIdentifier ?: ""
                    )
                )
            }
            viewModelScope.launch {
                delay(2500)
                _uiState.update { it.copy(lastScanResult = ScanUiResult.Idle) }
            }
            return
        }

        isProcessing = true
        viewModelScope.launch {
            val result = eventRepository.processScan(eventId, qrCodeHash)

            val scanUiResult = when (result) {
                is ScanResult.Success -> {
                    lastScannedQrCode = qrCodeHash
                    lastScannedIdentifier = result.attendee.uniqueIdentifier
                    ScanUiResult.Success(result.attendee.uniqueIdentifier)
                }

                is ScanResult.AttendeeNotFound -> {
                    lastScannedQrCode = null
                    lastScannedIdentifier = null
                    ScanUiResult.Error("Attendee Not Found")
                }

                is ScanResult.AlreadyScanned -> {
                    ScanUiResult.AlreadyScanned(lastScannedIdentifier ?: "")
                }
            }

            _uiState.update { it.copy(lastScanResult = scanUiResult) }

            delay(2500)
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
}
