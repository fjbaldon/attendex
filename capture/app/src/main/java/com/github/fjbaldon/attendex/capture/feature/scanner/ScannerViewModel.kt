package com.github.fjbaldon.attendex.capture.feature.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.core.data.remote.SessionResponse
import com.github.fjbaldon.attendex.capture.core.services.TtsService
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.github.fjbaldon.attendex.capture.data.event.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.abs

sealed class ScanUiResult {
    data object Idle : ScanUiResult()
    data class Success(val attendeeDetails: String) : ScanUiResult()
    data class Error(val message: String) : ScanUiResult()
    data class AlreadyScanned(val identifier: String) : ScanUiResult()
    data object ScanningInactive : ScanUiResult()
    data object SessionNotSelected : ScanUiResult()
}

data class ScannerUiState(
    val eventName: String? = null,
    val isLoading: Boolean = true,
    val lastScanResult: ScanUiResult = ScanUiResult.Idle,
    val scannedAttendees: List<AttendeeEntity> = emptyList(),
    val hasFlashUnit: Boolean = false,
    val isTorchOn: Boolean = false,
    val isEventActive: Boolean = true,
    val availableSessions: List<SessionResponse> = emptyList(),
    val selectedSession: SessionResponse? = null
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

    private val _availableSessions = MutableStateFlow<List<SessionResponse>>(emptyList())
    private val _selectedSession = MutableStateFlow<SessionResponse?>(null)

    private val scannedAttendeesFlow: Flow<List<AttendeeEntity>> =
        eventRepository.getScannedAttendeesStream(eventId)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ScannerUiState> = combine(
        _isLoading,
        _lastScanResult,
        scannedAttendeesFlow,
        _torchState,
        _eventName,
        _isEventActive,
        _availableSessions,
        _selectedSession
    ) { flows: Array<Any?> ->
        ScannerUiState(
            isLoading = flows[0] as Boolean,
            lastScanResult = flows[1] as ScanUiResult,
            scannedAttendees = flows[2] as List<AttendeeEntity>,
            hasFlashUnit = (flows[3] as Pair<Boolean, Boolean>).first,
            isTorchOn = (flows[3] as Pair<Boolean, Boolean>).second,
            eventName = flows[4] as String?,
            isEventActive = flows[5] as Boolean,
            availableSessions = flows[6] as List<SessionResponse>,
            selectedSession = flows[7] as SessionResponse?
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

            val sessions = eventRepository.getSessionsForEvent(eventId)
            _availableSessions.value = sessions

            // Automatically select the session closest to the current time
            updateSelectedSessionBasedOnTime(sessions)

            eventRepository.primeLastScannedIdentifier(eventId)
            _isLoading.value = false
        }
    }

    private fun updateSelectedSessionBasedOnTime(sessions: List<SessionResponse>) {
        if (sessions.isEmpty()) {
            _selectedSession.value = null
            return
        }

        val now = Instant.now()

        // Logic: Find the session where the absolute difference between NOW and TargetTime is smallest
        val bestSession = sessions.minByOrNull { session ->
            try {
                val target = Instant.parse(session.targetTime)
                abs(ChronoUnit.SECONDS.between(target, now))
            } catch (_: Exception) {
                Long.MAX_VALUE // Push invalid dates to the end
            }
        }

        _selectedSession.value = bestSession
    }

    fun processScannedText(scannedText: String) {
        updateSelectedSessionBasedOnTime(_availableSessions.value)

        val currentSession = _selectedSession.value
        if (currentSession == null) {
            _lastScanResult.value = ScanUiResult.SessionNotSelected
            resetScanResultAfterDelay()
            return
        }

        if (!_isEventActive.value || !isProcessing.compareAndSet(false, true)) return

        viewModelScope.launch {
            when (val result = eventRepository.processScan(eventId, currentSession.id, scannedText)) {
                is ScanResult.Success -> {
                    ttsService.speak(result.attendee.lastName)
                    _lastScanResult.value = ScanUiResult.Success(
                        attendeeDetails = "${result.attendee.firstName} ${result.attendee.lastName}"
                    )
                }

                is ScanResult.AlreadyScanned -> {
                    _lastScanResult.value = ScanUiResult.AlreadyScanned(scannedText)
                }

                is ScanResult.AttendeeNotFound -> {
                    // Optional: Handle "Not on Roster" specific UI state
                }
            }
            resetScanResultAfterDelay()
        }
    }

    private fun resetScanResultAfterDelay() {
        viewModelScope.launch {
            delay(250)
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
