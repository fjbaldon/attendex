package com.github.fjbaldon.attendex.platform.capture.dto;

import java.util.List;

public record BatchSyncResponse(
        int successCount,
        int failedCount,
        List<String> failedUuids // The mobile app needs these to stop retrying them
) {
}
