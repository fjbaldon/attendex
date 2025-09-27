package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.ActiveEventResponse;
import com.github.fjbaldon.attendex.backend.dto.AttendanceSyncRequest;
import com.github.fjbaldon.attendex.backend.dto.EventAttendeeSyncResponse;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCAN_ATTENDANCE')")
public class AppController {

    private final AppService appService;

    @GetMapping("/events")
    public ResponseEntity<List<ActiveEventResponse>> getActiveEvents(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(appService.getActiveEvents(user.getUsername()));
    }

    @GetMapping("/events/{eventId}/attendees")
    public ResponseEntity<List<EventAttendeeSyncResponse>> getEventAttendees(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(appService.getAttendeesForEvent(eventId, user.getUsername()));
    }

    @PostMapping("/attendance/sync")
    public ResponseEntity<Void> syncAttendanceData(
            @RequestBody AttendanceSyncRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        appService.syncAttendance(request, user.getUsername());
        return ResponseEntity.ok().build();
    }
}
