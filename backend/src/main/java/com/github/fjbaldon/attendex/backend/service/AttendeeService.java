package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.AttendeeImportAnalysisDto;
import com.github.fjbaldon.attendex.backend.dto.AttendeeRequest;
import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.dto.PaginatedResponseDto;
import com.github.fjbaldon.attendex.backend.exception.CsvValidationException;
import com.github.fjbaldon.attendex.backend.model.Attendee;
import com.github.fjbaldon.attendex.backend.model.CustomFieldDefinition;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import com.github.fjbaldon.attendex.backend.repository.CustomFieldDefinitionRepository;
import com.github.fjbaldon.attendex.backend.repository.OrganizationRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public AttendeeResponse createAttendee(AttendeeRequest request, Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        validateIdentifier(request.getUniqueIdentifier(), organization);

        Attendee attendee = new Attendee();
        attendee.setOrganization(organization);
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

        validateIdentifier(request.getUniqueIdentifier(), attendee.getOrganization());

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
    public void commitAttendees(List<AttendeeRequest> attendees, Long organizationId) {
        if (attendees == null || attendees.isEmpty()) {
            return;
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        List<Attendee> attendeesToSave = attendees.stream()
                .map(dto -> {
                    Attendee attendee = new Attendee();
                    attendee.setOrganization(organization);
                    attendee.setUniqueIdentifier(dto.getUniqueIdentifier());
                    attendee.setFirstName(dto.getFirstName());
                    attendee.setLastName(dto.getLastName());
                    attendee.setCustomFields(dto.getCustomFields());
                    return attendee;
                })
                .toList();

        attendeeRepository.saveAll(attendeesToSave);
    }

    @Transactional(readOnly = true)
    public AttendeeImportAnalysisDto analyzeAttendeesFromCsv(MultipartFile file, Long organizationId) {
        return parseAndValidateCsv(file, organizationId);
    }

    private AttendeeImportAnalysisDto parseAndValidateCsv(MultipartFile file, Long organizationId) {
        if (file.isEmpty()) {
            throw new CsvValidationException("Cannot import an empty file.");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        List<CustomFieldDefinition> customFieldDefinitions = customFieldDefinitionRepository.findByOrganizationId(organizationId);
        Map<String, CustomFieldDefinition> headerToDefMap = customFieldDefinitions.stream()
                .collect(Collectors.toMap(def -> def.getFieldName().toLowerCase(), Function.identity()));

        Set<String> existingIdentifiers = attendeeRepository.findAllByOrganizationId(organizationId, Pageable.unpaged())
                .stream()
                .map(Attendee::getUniqueIdentifier)
                .collect(Collectors.toSet());

        Set<String> identifiersInThisFile = new HashSet<>();
        List<AttendeeRequest> validAttendees = new ArrayList<>();
        List<AttendeeImportAnalysisDto.InvalidRowDto> invalidRows = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get();
            CSVParser csvParser = CSVParser.parse(fileReader, csvFormat);

            List<String> headers = csvParser.getHeaderNames();
            String uniqueIdentifierHeader = headers.stream().filter(h -> h.equalsIgnoreCase("uniqueIdentifier")).findFirst().orElse("uniqueIdentifier");
            String firstNameHeader = headers.stream().filter(h -> h.equalsIgnoreCase("firstName")).findFirst().orElse("firstName");
            String lastNameHeader = headers.stream().filter(h -> h.equalsIgnoreCase("lastName")).findFirst().orElse("lastName");

            for (CSVRecord csvRecord : csvParser) {
                try {
                    String uniqueIdentifier = csvRecord.get(uniqueIdentifierHeader);
                    if (!StringUtils.hasText(uniqueIdentifier)) {
                        throw new IllegalStateException("Missing required value for 'uniqueIdentifier'.");
                    }

                    validateIdentifier(uniqueIdentifier, organization);

                    if (existingIdentifiers.contains(uniqueIdentifier) || identifiersInThisFile.contains(uniqueIdentifier)) {
                        throw new IllegalStateException("Duplicate identifier '" + uniqueIdentifier + "' found.");
                    }
                    identifiersInThisFile.add(uniqueIdentifier);

                    AttendeeRequest attendeeRequest = new AttendeeRequest();
                    attendeeRequest.setUniqueIdentifier(uniqueIdentifier);
                    attendeeRequest.setFirstName(csvRecord.get(firstNameHeader));
                    attendeeRequest.setLastName(csvRecord.get(lastNameHeader));

                    Map<String, Object> customFields = new HashMap<>();
                    for (String header : headers) {
                        CustomFieldDefinition fieldDef = headerToDefMap.get(header.toLowerCase());

                        if (fieldDef != null && csvRecord.isMapped(header) && StringUtils.hasText(csvRecord.get(header))) {
                            String value = csvRecord.get(header);
                            String fieldName = fieldDef.getFieldName();

                            switch (fieldDef.getFieldType()) {
                                case SELECT:
                                    List<String> options = fieldDef.getOptions();
                                    if (options == null || options.isEmpty()) {
                                        throw new IllegalStateException("Field '" + fieldName + "' is a SELECT type but has no options configured.");
                                    }
                                    if (!options.contains(value)) {
                                        throw new IllegalStateException("Invalid value '" + value + "' for field '" + fieldName + "'. Allowed values are: " + String.join(", ", options));
                                    }
                                    customFields.put(fieldName, value);
                                    break;
                                case NUMBER:
                                    try {
                                        customFields.put(fieldName, Double.parseDouble(value));
                                    } catch (NumberFormatException e) {
                                        throw new IllegalStateException("Invalid number format for field '" + fieldName + "'. Value was: '" + value + "'.");
                                    }
                                    break;
                                case DATE:
                                    try {
                                        customFields.put(fieldName, LocalDate.parse(value).toString());
                                    } catch (DateTimeParseException e) {
                                        throw new IllegalStateException("Invalid date format for field '" + fieldName + "'. Please use YYYY-MM-DD format. Value was: '" + value + "'.");
                                    }
                                    break;
                                case TEXT:
                                default:
                                    customFields.put(fieldName, value);
                                    break;
                            }
                        }
                    }
                    attendeeRequest.setCustomFields(customFields);

                    validAttendees.add(attendeeRequest);
                } catch (Exception e) {
                    invalidRows.add(AttendeeImportAnalysisDto.InvalidRowDto.builder()
                            .rowNumber(csvRecord.getRecordNumber() + 1) // +1 to account for header
                            .rowData(csvRecord.toMap())
                            .error(e.getMessage())
                            .build());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }

        return AttendeeImportAnalysisDto.builder()
                .validAttendees(validAttendees)
                .invalidRows(invalidRows)
                .build();
    }

    private void validateIdentifier(String identifier, Organization organization) {
        String regex = organization.getIdentifierFormatRegex();
        if (StringUtils.hasText(regex)) {
            if (!identifier.matches(regex)) {
                throw new IllegalArgumentException("Identifier '" + identifier + "' does not match the required format for this organization.");
            }
        }
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
