package com.github.fjbaldon.attendex.capture.data.event

import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity

sealed class ScanResult {
    data class Success(val attendee: AttendeeEntity, val scanMessage: String) : ScanResult()
    data object AttendeeNotFound : ScanResult()
    data class AlreadyScanned(val attendee: AttendeeEntity) : ScanResult()
}
