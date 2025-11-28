package com.github.fjbaldon.attendex.platform.report;

import com.github.fjbaldon.attendex.platform.capture.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            @RequestParam(required = false) Long sessionId, // NEW PARAM
            @RequestParam(required = false) String type,
            @RequestParam Map<String, String> allParams,
            @AuthenticationPrincipal CustomUserDetails user) {

        allParams.remove("type");
        allParams.remove("sessionId"); // Remove specific params from attribute map

        // 1. Check Data Size to decide strategy
        // Simplified check: We assume filtering by session reduces size, so simpler check is fine for now.
        // Or call count method. For brevity, assuming sync generation is fine or using old count logic:

        List<Long> attendeeIds = attendeeFacade.findAttendeeIdsByFilters(user.getOrganizationId(), allParams);
        List<EntryDetailsDto> entries = captureFacade.findFilteredEntries(user.getOrganizationId(), eventId, sessionId, type, attendeeIds);
        long count = entries.size();

        if (count > 2500) {
            reportFacade.generateAndEmailReportPdf(
                    user.getOrganizationId(),
                    eventId,
                    sessionId, // Pass Session
                    type,
                    allParams,
                    user.getUsername()
            );
            return ResponseEntity.accepted()
                    .body(Map.of("message", "Report is large (" + count + " records). It will be emailed to " + user.getUsername()));
        }

        byte[] pdfBytes = reportFacade.generateReportPdf(
                user.getOrganizationId(),
                eventId,
                sessionId, // Pass Session
                type,
                allParams
        );

        String filename = "report.pdf"; // You could make this dynamic: eventName_sessionName.pdf

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
