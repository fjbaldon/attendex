package com.github.fjbaldon.attendex.platform.capture;

import java.util.List;

public record BatchSyncResponse(
        int successCount,
        int failedCount,
        List<String> failedUuids // The mobile app needs these to stop retrying them
) {
}
