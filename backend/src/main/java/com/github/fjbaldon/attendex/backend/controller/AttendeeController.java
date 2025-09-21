package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.AttendeeImportResponse;
import com.github.fjbaldon.attendex.backend.dto.AttendeeRequest;
import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.service.AttendeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/attendees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class AttendeeController {

    private final AttendeeService attendeeService;

    @PostMapping
    public ResponseEntity<AttendeeResponse> createAttendee(@Valid @RequestBody AttendeeRequest request) {
        return new ResponseEntity<>(attendeeService.createAttendee(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<AttendeeResponse>> getAllAttendees(Pageable pageable) {
        return ResponseEntity.ok(attendeeService.getAllAttendees(pageable));
    }

    @PutMapping("/{attendeeId}")
    public ResponseEntity<AttendeeResponse> updateAttendee(@PathVariable Long attendeeId, @Valid @RequestBody AttendeeRequest request) {
        return ResponseEntity.ok(attendeeService.updateAttendee(attendeeId, request));
    }

    @DeleteMapping("/{attendeeId}")
    public ResponseEntity<Void> deleteAttendee(@PathVariable Long attendeeId) {
        attendeeService.deleteAttendee(attendeeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<AttendeeImportResponse> importAttendees(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(attendeeService.importAttendeesFromCsv(file));
    }
}
