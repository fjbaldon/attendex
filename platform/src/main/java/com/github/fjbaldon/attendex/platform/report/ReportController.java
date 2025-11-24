package com.github.fjbaldon.attendex.platform.report;

import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
class ReportController {

    private final ReportFacade reportFacade;
    private final com.github.fjbaldon.attendex.platform.capture.CaptureFacade captureFacade;
    private final com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade attendeeFacade;

    @GetMapping("/events/{eventId}/pdf")
    public ResponseEntity<?> generateSmartPdf(
            @PathVariable Long eventId,
            @RequestParam(required = false) String type,
            @RequestParam Map<String, String> allParams,
            @AuthenticationPrincipal CustomUserDetails user) {

        allParams.remove("type");

        // 1. Check Data Size to decide strategy
        long count;
        if (allParams.isEmpty() && (type == null || type.equals("All"))) {
            // Fast path: use pre-calculated stats if no filters
            count = captureFacade.getEventStats(user.getOrganizationId(), eventId).totalScans();
        } else {
            // Slow path: calculate filtered set size
            var attendeeIds = attendeeFacade.findAttendeeIdsByFilters(user.getOrganizationId(), allParams);
            var entries = captureFacade.findFilteredEntries(user.getOrganizationId(), eventId, type, attendeeIds);
            count = entries.size();
        }

        // 2. Decision Threshold (2500 Records)
        if (count > 2500) {
            // Too big. Switch to Email Mode.
            reportFacade.generateAndEmailReportPdf(
                    user.getOrganizationId(),
                    eventId,
                    type,
                    allParams,
                    user.getUsername()
            );
            // Return 202 ACCEPTED to tell frontend "We are working on it"
            return ResponseEntity.accepted()
                    .body(Map.of("message", "Report is large (" + count + " records). It will be emailed to " + user.getUsername()));
        }

        // 3. Small enough. Download Mode (Synchronous).
        byte[] pdfBytes = reportFacade.generateReportPdf(
                user.getOrganizationId(),
                eventId,
                type,
                allParams
        );

        String filename = "report.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
