package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.attendee.dto.*;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendees")
@RequiredArgsConstructor
class AttendeeController {
    private final AttendeeFacade attendeeFacade;

    @PostMapping
    public ResponseEntity<AttendeeDto> createAttendee(
            @Valid @RequestBody CreateAttendeeDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        AttendeeDto attendee = attendeeFacade.createAttendee(user.getOrganizationId(), request);
        return new ResponseEntity<>(attendee, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<AttendeeDto> getAttendees(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return attendeeFacade.findAttendees(user.getOrganizationId(), pageable);
    }

    @PostMapping("/import/analyze")
    public ResponseEntity<AttendeeImportAnalysisDto> analyzeImport(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails user) throws IOException {
        return ResponseEntity.ok(attendeeFacade.analyzeAttendeeImport(user.getOrganizationId(), file));
    }

    @PostMapping("/import/commit")
    @ResponseStatus(HttpStatus.CREATED)
    public void commitImport(
            @Valid @RequestBody AttendeeImportCommitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        attendeeFacade.commitAttendeeImport(user.getOrganizationId(), request.attendees());
    }

    @PutMapping("/{attendeeId}")
    public ResponseEntity<AttendeeDto> updateAttendee(
            @PathVariable Long attendeeId,
            @Valid @RequestBody UpdateAttendeeDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        AttendeeDto attendee = attendeeFacade.updateAttendee(user.getOrganizationId(), attendeeId, request);
        return ResponseEntity.ok(attendee);
    }

    @PostMapping("/attributes")
    public ResponseEntity<AttributeDto> createAttribute(
            @Valid @RequestBody CreateAttributeDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        AttributeDto attribute = attendeeFacade.createAttribute(user.getOrganizationId(), request);
        return new ResponseEntity<>(attribute, HttpStatus.CREATED);
    }

    @GetMapping("/attributes")
    public List<AttributeDto> getAttributes(@AuthenticationPrincipal CustomUserDetails user) {
        return attendeeFacade.findAttributes(user.getOrganizationId());
    }
}
