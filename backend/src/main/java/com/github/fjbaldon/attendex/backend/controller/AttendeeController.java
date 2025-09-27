package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.AttendeeImportResponse;
import com.github.fjbaldon.attendex.backend.dto.AttendeeRequest;
import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.AttendeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/attendees")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_ATTENDEES')")
public class AttendeeController {

    private final AttendeeService attendeeService;

    @PostMapping
    public ResponseEntity<AttendeeResponse> createAttendee(@Valid @RequestBody AttendeeRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        return new ResponseEntity<>(attendeeService.createAttendee(request, user.getOrganizationId()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<AttendeeResponse>> getAllAttendees(Pageable pageable, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(attendeeService.getAllAttendees(user.getOrganizationId(), pageable));
    }

    @PutMapping("/{attendeeId}")
    public ResponseEntity<AttendeeResponse> updateAttendee(@PathVariable Long attendeeId, @Valid @RequestBody AttendeeRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(attendeeService.updateAttendee(attendeeId, request, user.getOrganizationId()));
    }

    @DeleteMapping("/{attendeeId}")
    public ResponseEntity<Void> deleteAttendee(@PathVariable Long attendeeId, @AuthenticationPrincipal CustomUserDetails user) {
        attendeeService.deleteAttendee(attendeeId, user.getOrganizationId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<AttendeeImportResponse> importAttendees(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(attendeeService.importAttendeesFromCsv(file, user.getOrganizationId()));
    }
}
