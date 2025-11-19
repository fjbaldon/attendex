package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.attendee.dto.*;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
            @RequestParam(required = false) String query,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return attendeeFacade.findAttendees(user.getOrganizationId(), query, pageable);
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

    @GetMapping("/import-template")
    public ResponseEntity<String> getImportTemplate(@AuthenticationPrincipal CustomUserDetails user) {
        String csvContent = attendeeFacade.generateImportTemplate(user.getOrganizationId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendee_template.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvContent);
    }

    @PutMapping("/attributes/{attributeId}")
    public ResponseEntity<AttributeDto> updateAttribute(
            @PathVariable Long attributeId,
            @Valid @RequestBody CreateAttributeDto request, // Reusing Create DTO for simplicity
            @AuthenticationPrincipal CustomUserDetails user) {
        // We only update options, ignoring name changes as per business rule
        AttributeDto attribute = attendeeFacade.updateAttribute(user.getOrganizationId(), attributeId, request.options());
        return ResponseEntity.ok(attribute);
    }

    @DeleteMapping("/attributes/{attributeId}")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable Long attributeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        attendeeFacade.deleteAttribute(user.getOrganizationId(), attributeId);
        return ResponseEntity.noContent().build();
    }
}
