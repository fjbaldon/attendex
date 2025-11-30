package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
class CaptureEventQueryController {

    private final CaptureFacade captureFacade;

    @GetMapping("/{eventId}/entries")
    public Page<EntryDetailsDto> getEntries(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String intent,
            @RequestParam(required = false) String query,
            @RequestParam Map<String, String> allParams,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {

        Map<String, String> filters = new HashMap<>(allParams);
        // Clean up known params to leave only attribute filters
        filters.remove("sessionId");
        filters.remove("intent");
        filters.remove("query");
        filters.remove("page");
        filters.remove("size");
        filters.remove("sort");

        return captureFacade.findEntries(eventId, user.getOrganizationId(), sessionId, intent, query, filters, pageable);
    }
}
