package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.capture.dto.EntrySyncRequestDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EventSyncDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.dto.SessionDetailsDto;
import com.github.fjbaldon.attendex.platform.event.dto.SessionEventDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/capture")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCANNER')")
class CaptureController {

    private final CaptureFacade captureFacade;
    private final EventFacade eventFacade;

    @GetMapping("/events")
    public ResponseEntity<List<EventSyncDto>> getEventsForSync(@AuthenticationPrincipal CustomUserDetails user) {
        List<EventSyncDto> events = eventFacade.getEventsForSync(user.getOrganizationId());
        return ResponseEntity.ok(events);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncEntries(
            @Valid @RequestBody EntrySyncRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        Set<Long> sessionIds = request.records().stream()
                .map(EntrySyncRequestDto.EntryRecord::sessionId)
                .collect(Collectors.toSet());

        Map<Long, SessionDetailsDto> sessionDetailsMap = eventFacade.findSessionDetailsByIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionDetailsDto::sessionId, Function.identity()));

        Map<Long, Long> sessionIdToEventIdMap = eventFacade.findEventsForSessionIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionEventDto::sessionId, SessionEventDto::eventId));

        captureFacade.syncEntries(
                user.getOrganizationId(),
                user.getUsername(),
                request,
                sessionDetailsMap,
                sessionIdToEventIdMap
        );

        return ResponseEntity.ok().build();
    }
}
