package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BatchSyncResponse(
    val successCount: Int,
    val failedCount: Int,
    val failedUuids: List<String>
)
