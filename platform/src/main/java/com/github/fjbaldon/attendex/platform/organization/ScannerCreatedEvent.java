package com.github.fjbaldon.attendex.platform.organization;

public record ScannerCreatedEvent(
        Long scannerId,
        Long organizationId
) {
}
