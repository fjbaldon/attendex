package com.github.fjbaldon.attendex.platform.organization.events;

public record ScannerCreatedEvent(
        Long scannerId,
        Long organizationId
) {
}
