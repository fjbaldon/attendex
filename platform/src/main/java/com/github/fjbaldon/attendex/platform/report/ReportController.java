package com.github.fjbaldon.attendex.platform.report;

import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
class ReportController {

    private final ReportFacade reportFacade;

    // NEW ENDPOINT
    @GetMapping("/events/{eventId}/pdf")
    public ResponseEntity<byte[]> downloadArrivalsPdf(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails user) {

        byte[] pdfBytes = reportFacade.generateArrivalsPdf(user.getOrganizationId(), eventId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=arrivals_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
