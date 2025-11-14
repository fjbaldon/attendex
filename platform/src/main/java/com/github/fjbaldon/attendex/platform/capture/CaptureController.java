package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.capture.dto.EntrySyncRequestDto;
import com.github.fjbaldon.attendex.platform.capture.dto.EventSyncDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capture")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCANNER')")
class CaptureController {

    private final CaptureFacade captureFacade;

    @GetMapping("/events")
    public ResponseEntity<List<EventSyncDto>> getEventsForSync(@AuthenticationPrincipal CustomUserDetails user) {
        List<EventSyncDto> events = captureFacade.getEventsForSync(user.getOrganizationId());
        return ResponseEntity.ok(events);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncEntries(
            @Valid @RequestBody EntrySyncRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        captureFacade.syncEntries(user.getOrganizationId(), user.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
