package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
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
            @RequestParam Map<String, String> allParams, // Capture everything
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {

        // Extract attribute filters by removing known params
        Map<String, String> filters = new HashMap<>(allParams);
        filters.remove("page");
        filters.remove("size");
        filters.remove("sort");
        filters.remove("query");

        return attendeeFacade.findAttendees(user.getOrganizationId(), query, filters, pageable);
    }

    @PutMapping("/{attendeeId}")
    public ResponseEntity<AttendeeDto> updateAttendee(
            @PathVariable Long attendeeId,
            @Valid @RequestBody UpdateAttendeeDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        AttendeeDto attendee = attendeeFacade.updateAttendee(user.getOrganizationId(), attendeeId, request);
        return ResponseEntity.ok(attendee);
    }

    @DeleteMapping("/{attendeeId}")
    public ResponseEntity<Void> deleteAttendee(
            @PathVariable Long attendeeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        attendeeFacade.deleteAttendee(user.getOrganizationId(), attendeeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/headers")
    public ResponseEntity<List<String>> getCsvHeaders(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(attendeeFacade.extractCsvHeaders(file));
    }

    @PostMapping(value = "/import/analyze", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AttendeeImportAnalysisDto> analyzeAttendees(
            @RequestPart("file") MultipartFile file,
            @RequestPart("config") ImportConfigurationDto config,
            @AuthenticationPrincipal CustomUserDetails user) throws IOException {
        return ResponseEntity.ok(attendeeFacade.analyzeAttendeeImport(user.getOrganizationId(), file, config));
    }

    @PostMapping("/import/commit")
    public ResponseEntity<Void> commitAttendees(
            @Valid @RequestBody AttendeeImportCommitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        attendeeFacade.commitAttendeeImport(
                user.getOrganizationId(),
                request.attendees(),
                request.updateExisting(),
                request.newAttributes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/import-template")
    public ResponseEntity<String> getImportTemplate(@AuthenticationPrincipal CustomUserDetails user) {
        String csvContent = attendeeFacade.generateImportTemplate(user.getOrganizationId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendee_template.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvContent);
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

    @PutMapping("/attributes/{attributeId}")
    public ResponseEntity<AttributeDto> updateAttribute(
            @PathVariable Long attributeId,
            @Valid @RequestBody CreateAttributeDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        AttributeDto attribute = attendeeFacade.updateAttribute(
                user.getOrganizationId(),
                attributeId,
                request.name(),
                request.options()
        );
        return ResponseEntity.ok(attribute);
    }

    @DeleteMapping("/attributes/{attributeId}")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable Long attributeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        attendeeFacade.deleteAttribute(user.getOrganizationId(), attributeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAttendees(
            @Valid @RequestBody BatchDeleteRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        attendeeFacade.deleteAttendees(user.getOrganizationId(), request.ids());
        return ResponseEntity.noContent().build();
    }
}
