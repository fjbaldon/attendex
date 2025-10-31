package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.*;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.AttendeeService;
import com.github.fjbaldon.attendex.backend.service.CustomFieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/attendees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class AttendeeController {

    private final AttendeeService attendeeService;
    private final CustomFieldService customFieldService;

    @PostMapping
    public ResponseEntity<AttendeeResponse> createAttendee(@Valid @RequestBody AttendeeRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        return new ResponseEntity<>(attendeeService.createAttendee(request, user.getOrganizationId()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<AttendeeResponse>> getAllAttendees(Pageable pageable, @AuthenticationPrincipal CustomUserDetails user) {
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

    @PostMapping("/import/analyze")
    public ResponseEntity<AttendeeImportAnalysisDto> analyzeAttendees(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(attendeeService.analyzeAttendeesFromCsv(file, user.getOrganizationId()));
    }

    @PostMapping("/import/commit")
    public ResponseEntity<Void> commitAttendees(@Valid @RequestBody AttendeeImportCommitRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        attendeeService.commitAttendees(request.getAttendees(), user.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/import-template")
    public ResponseEntity<String> getImportTemplate(@AuthenticationPrincipal CustomUserDetails user) {
        List<CustomFieldDefinitionDto> customFields = customFieldService.getDefinitionsByOrganization(user.getOrganizationId());

        String customFieldHeaders = customFields.stream()
                .map(CustomFieldDefinitionDto::getFieldName)
                .collect(Collectors.joining(","));

        String csvHeader = "uniqueIdentifier,firstName,lastName" + (customFieldHeaders.isEmpty() ? "" : "," + customFieldHeaders);
        String csvExampleRow = "2024001,John,Smith" + (customFields.isEmpty() ? "" : "," + customFields.stream().map(cf -> "SampleValue").collect(Collectors.joining(",")));

        String csvContent = csvHeader + "\n" + csvExampleRow + "\n";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendee_template.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }
}
