package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.AttendeeImportResponse;
import com.github.fjbaldon.attendex.backend.dto.AttendeeRequest;
import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.dto.PaginatedResponseDto;
import com.github.fjbaldon.attendex.backend.exception.CsvValidationException;
import com.github.fjbaldon.attendex.backend.model.Attendee;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;

    @Transactional
    public AttendeeResponse createAttendee(AttendeeRequest request, Long organizationId) {
        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        Attendee attendee = new Attendee();
        attendee.setOrganization(orgReference);
        attendee.setUniqueIdentifier(request.getUniqueIdentifier());
        attendee.setFirstName(request.getFirstName());
        attendee.setLastName(request.getLastName());
        attendee.setCustomFields(request.getCustomFields());

        Attendee savedAttendee = attendeeRepository.save(attendee);
        return toAttendeeResponse(savedAttendee);
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDto<AttendeeResponse> getAllAttendees(Long organizationId, Pageable pageable) {
        Page<AttendeeResponse> page = attendeeRepository.findAllByOrganizationId(organizationId, pageable)
                .map(this::toAttendeeResponse);
        return new PaginatedResponseDto<>(page);
    }

    @Transactional
    public AttendeeResponse updateAttendee(Long attendeeId, AttendeeRequest request, Long organizationId) {
        Attendee attendee = findAttendeeByIdAndOrgId(attendeeId, organizationId);

        attendee.setUniqueIdentifier(request.getUniqueIdentifier());
        attendee.setFirstName(request.getFirstName());
        attendee.setLastName(request.getLastName());
        attendee.setCustomFields(request.getCustomFields());

        Attendee updatedAttendee = attendeeRepository.save(attendee);
        return toAttendeeResponse(updatedAttendee);
    }

    @Transactional
    public void deleteAttendee(Long attendeeId, Long organizationId) {
        Attendee attendee = findAttendeeByIdAndOrgId(attendeeId, organizationId);
        attendeeRepository.delete(attendee);
    }

    @Transactional
    public AttendeeImportResponse importAttendeesFromCsv(MultipartFile file, Long organizationId) {
        if (file.isEmpty()) {
            throw new CsvValidationException("Cannot import an empty file.");
        }

        List<Attendee> attendeesToSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;

        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get();
            CSVParser csvParser = CSVParser.parse(fileReader, csvFormat);

            List<String> headers = csvParser.getHeaderNames();
            List<String> requiredHeaders = Arrays.asList("uniqueIdentifier", "firstName", "lastName");
            List<String> missingHeaders = requiredHeaders.stream()
                    .filter(requiredHeader -> !headers.contains(requiredHeader))
                    .collect(Collectors.toList());

            if (!missingHeaders.isEmpty()) {
                throw new CsvValidationException("CSV file is missing required columns: " + String.join(", ", missingHeaders));
            }

            for (CSVRecord csvRecord : csvParser) {
                try {
                    String uniqueIdentifier = csvRecord.get("uniqueIdentifier");
                    String firstName = csvRecord.get("firstName");
                    String lastName = csvRecord.get("lastName");

                    if (!StringUtils.hasText(uniqueIdentifier) || !StringUtils.hasText(firstName) || !StringUtils.hasText(lastName)) {
                        throw new IllegalStateException("Required fields (uniqueIdentifier, firstName, lastName) cannot be empty.");
                    }

                    Attendee attendee = new Attendee();
                    attendee.setOrganization(orgReference);
                    attendee.setUniqueIdentifier(uniqueIdentifier);
                    attendee.setFirstName(firstName);
                    attendee.setLastName(lastName);

                    Map<String, Object> customFields = new HashMap<>();
                    for (String header : headers) {
                        if (!requiredHeaders.contains(header)) {
                            if (csvRecord.isMapped(header) && StringUtils.hasText(csvRecord.get(header))) {
                                customFields.put(header, csvRecord.get(header));
                            }
                        }
                    }
                    attendee.setCustomFields(customFields);

                    attendeesToSave.add(attendee);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                    errors.add("Error on row " + csvRecord.getRecordNumber() + ": " + e.getMessage());
                }
            }

            if (failedImports > 0 && successfulImports == 0 && !errors.isEmpty()) {
                throw new CsvValidationException("Failed to process any rows. Please check file content. First error: " + errors.get(0));
            }

            attendeeRepository.saveAll(attendeesToSave);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }

        return AttendeeImportResponse.builder()
                .message("CSV processing complete.")
                .successfulImports(successfulImports)
                .failedImports(failedImports)
                .errors(errors)
                .build();
    }

    private Attendee findAttendeeByIdAndOrgId(Long attendeeId, Long organizationId) {
        return attendeeRepository.findById(attendeeId)
                .filter(a -> a.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Attendee with ID " + attendeeId + " not found in your organization"));
    }

    private AttendeeResponse toAttendeeResponse(Attendee attendee) {
        return AttendeeResponse.builder()
                .id(attendee.getId())
                .uniqueIdentifier(attendee.getUniqueIdentifier())
                .firstName(attendee.getFirstName())
                .lastName(attendee.getLastName())
                .customFields(attendee.getCustomFields())
                .build();
    }
}
