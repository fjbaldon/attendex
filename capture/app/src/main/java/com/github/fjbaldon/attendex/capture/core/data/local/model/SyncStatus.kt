package com.github.fjbaldon.attendex.capture.core.data.local.model

enum class SyncStatus {
    PENDING, // Captured locally, waiting for sync
    SYNCED,  // Successfully uploaded to server
    FAILED   // Permanent failure (e.g., data rejected) or retryable error
}
