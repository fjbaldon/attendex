package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.ActiveEventResponse;
import com.github.fjbaldon.attendex.backend.dto.AttendanceSyncRequest;
import com.github.fjbaldon.attendex.backend.dto.EventAttendeeSyncResponse;
import com.github.fjbaldon.attendex.backend.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCANNER')")
public class AppController {

    private final AppService appService;

    @GetMapping("/events")
    public ResponseEntity<List<ActiveEventResponse>> getActiveEvents(Principal principal) {
        return ResponseEntity.ok(appService.getActiveEvents(principal.getName()));
    }

    @GetMapping("/events/{eventId}/attendees")
    public ResponseEntity<List<EventAttendeeSyncResponse>> getEventAttendees(
            @PathVariable Long eventId,
            Principal principal) {
        return ResponseEntity.ok(appService.getAttendeesForEvent(eventId, principal.getName()));
    }

    @PostMapping("/attendance/sync")
    public ResponseEntity<Void> syncAttendanceData(
            @RequestBody AttendanceSyncRequest request,
            Principal principal) {
        appService.syncAttendance(request, principal.getName());
        return ResponseEntity.ok().build();
    }
}
