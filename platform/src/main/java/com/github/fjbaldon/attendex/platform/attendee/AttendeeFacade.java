package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.attendee.dto.*;
import com.github.fjbaldon.attendex.platform.attendee.events.AttendeeCreatedEvent;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AttendeeFacade {

    private final AttendeeRepository attendeeRepository;
    private final AttributeRepository attributeRepository;
    private final OrganizationFacade organizationFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AttendeeDto createAttendee(Long organizationId, CreateAttendeeDto dto) {
        Assert.isTrue(!attendeeRepository.existsByOrganizationIdAndIdentity(organizationId, dto.identity()),
                "An attendee with this identity already exists.");

        validateIdentityFormat(organizationId, dto.identity());

        Assert.isTrue(!attendeeRepository.existsByOrganizationIdAndIdentity(organizationId, dto.identity()),
                "An attendee with this identity already exists in your organization.");

        Attendee attendee = Attendee.create(
                organizationId,
                dto.identity(),
                dto.firstName(),
                dto.lastName(),
                dto.attributes()
        );

        Attendee saved = attendeeRepository.save(attendee);
        eventPublisher.publishEvent(new AttendeeCreatedEvent(saved.getId(), saved.getOrganizationId()));
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findAttendees(Long organizationId, String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return attendeeRepository.searchByOrganizationId(organizationId, query.trim(), pageable)
                    .map(this::toDto);
        }
        return attendeeRepository.findAllByOrganizationId(organizationId, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<AttendeeDto> findAttendeeById(Long attendeeId, Long organizationId) {
        return attendeeRepository.findById(attendeeId)
                .filter(attendee -> attendee.getOrganizationId().equals(organizationId))
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<AttendeeDto> findAttendeesByIds(List<Long> attendeeIds) {
        Iterable<Attendee> attendees = attendeeRepository.findAllById(attendeeIds);
        return StreamSupport.stream(attendees.spliterator(), false)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendeeDto updateAttendee(Long organizationId, Long attendeeId, UpdateAttendeeDto dto) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .filter(a -> a.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Attendee not found"));

        attendee.update(dto.firstName(), dto.lastName(), dto.attributes());
        return toDto(attendee);
    }

    @Transactional
    public AttributeDto createAttribute(Long organizationId, CreateAttributeDto dto) {
        Assert.isTrue(!attributeRepository.existsByOrganizationIdAndName(organizationId, dto.name()),
                "An attribute with this name already exists.");
        Attribute attribute = Attribute.create(organizationId, dto.name(), dto.type(), dto.options());
        Attribute saved = attributeRepository.save(attribute);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AttributeDto> findAttributes(Long organizationId) {
        return attributeRepository.findAllByOrganizationId(organizationId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttributeDto updateAttribute(Long organizationId, Long attributeId, List<String> newOptions) {
        Attribute attribute = attributeRepository.findByIdAndOrganizationId(attributeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));

        attribute.updateOptions(newOptions);
        return toDto(attributeRepository.save(attribute));
    }

    @Transactional
    public void deleteAttribute(Long organizationId, Long attributeId) {
        Attribute attribute = attributeRepository.findByIdAndOrganizationId(attributeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));

        if (attendeeRepository.isAttributeInUse(organizationId, attribute.getName())) {
            throw new IllegalStateException("Cannot delete attribute '" + attribute.getName() + "' because it is currently assigned to one or more attendees.");
        }

        attributeRepository.delete(attribute);
    }

    @Transactional(readOnly = true)
    public AttendeeImportAnalysisDto analyzeAttendeeImport(Long organizationId, MultipartFile file) throws IOException {
        OrganizationDto organization = organizationFacade.findOrganizationById(organizationId);
        String regex = organization.identityFormatRegex();

        List<Attribute> attributes = attributeRepository.findAllByOrganizationId(organizationId);
        Map<String, Attribute> attributeMap = attributes.stream()
                .collect(Collectors.toMap(attr -> attr.getName().toLowerCase(), Function.identity()));

        List<CreateAttendeeDto> validAttendees = new ArrayList<>();
        List<AttendeeImportAnalysisDto.InvalidRow> invalidRows = new ArrayList<>();
        Set<String> identifiersInThisFile = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .get();

            CSVParser csvParser = CSVParser.parse(reader, csvFormat);

            for (CSVRecord record : csvParser) {
                try {
                    String identity = record.isMapped("identity") ? record.get("identity") : null;
                    String firstName = record.isMapped("firstName") ? record.get("firstName") : null;
                    String lastName = record.isMapped("lastName") ? record.get("lastName") : null;

                    if (!StringUtils.hasText(identity)) throw new IllegalArgumentException("Missing 'identity'.");
                    if (!StringUtils.hasText(firstName)) throw new IllegalArgumentException("Missing 'firstName'.");
                    if (!StringUtils.hasText(lastName)) throw new IllegalArgumentException("Missing 'lastName'.");

                    if (regex != null && !regex.isBlank() && !identity.matches(regex)) {
                        throw new IllegalArgumentException("Identity does not match required format: " + regex);
                    }

                    if (attendeeRepository.existsByOrganizationIdAndIdentity(organizationId, identity)) {
                        throw new IllegalArgumentException("Identity '" + identity + "' already exists in the system.");
                    }
                    if (identifiersInThisFile.contains(identity)) {
                        throw new IllegalArgumentException("Duplicate identity '" + identity + "' found in this file.");
                    }
                    identifiersInThisFile.add(identity);

                    Map<String, Object> attributeValues = new HashMap<>();
                    for (String header : csvParser.getHeaderNames()) {
                        Attribute attribute = attributeMap.get(header.toLowerCase());

                        if (attribute != null && record.isMapped(header)) {
                            String value = record.get(header);
                            if (StringUtils.hasText(value)) {
                                if (attribute.getOptions() != null && !attribute.getOptions().contains(value)) {
                                    throw new IllegalStateException("Value '" + value + "' is invalid for attribute '" + attribute.getName() + "'. Allowed: " + attribute.getOptions());
                                }
                                attributeValues.put(attribute.getName(), value);
                            }
                        }
                    }

                    validAttendees.add(new CreateAttendeeDto(
                            identity,
                            firstName,
                            lastName,
                            attributeValues
                    ));

                } catch (Exception e) {
                    invalidRows.add(new AttendeeImportAnalysisDto.InvalidRow(
                            record.getRecordNumber(),
                            record.toMap(),
                            e.getMessage()
                    ));
                }
            }
        }

        return new AttendeeImportAnalysisDto(validAttendees, invalidRows);
    }

    @Transactional
    public void commitAttendeeImport(Long organizationId, List<CreateAttendeeDto> attendees) {
        List<Attendee> entitiesToSave = attendees.stream()
                .map(dto -> Attendee.create(organizationId, dto.identity(), dto.firstName(), dto.lastName(), dto.attributes()))
                .toList();
        attendeeRepository.saveAll(entitiesToSave);
    }

    @Transactional(readOnly = true)
    public long countAttendees(Long organizationId) {
        return attendeeRepository.countByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public String generateImportTemplate(Long organizationId) {
        List<Attribute> attributes = attributeRepository.findAllByOrganizationId(organizationId);

        StringBuilder csv = new StringBuilder("identity,firstName,lastName");
        for (Attribute attr : attributes) {
            csv.append(",").append(attr.getName());
        }
        csv.append("\n");

        csv.append("2025001,John,Doe");
        for (Attribute attr : attributes) {
            csv.append(",").append("SampleValue");
        }
        csv.append("\n");

        return csv.toString();
    }

    private void validateIdentityFormat(Long organizationId, String identity) {
        var org = organizationFacade.findOrganizationById(organizationId);
        // Note: 'identityFormatRegex' might be null/empty in DTO if not set
        String regex = null;
        // You might need to expose regex in OrganizationDto if not already there.
        // Assuming OrganizationDto has it (it does in your code).
        try {
            // Accessing private field via reflection or getter in DTO record
            regex = org.identityFormatRegex(); // Ensure OrganizationDto has this accessor
        } catch (Exception e) { /* handle or ignore */ }

        if (regex != null && !regex.isBlank() && !identity.matches(regex)) {
            throw new IllegalArgumentException("Identity '" + identity + "' does not match the required format: " + regex);
        }
    }

    private AttendeeDto toDto(Attendee attendee) {
        return new AttendeeDto(
                attendee.getId(),
                attendee.getIdentity(),
                attendee.getFirstName(),
                attendee.getLastName(),
                attendee.getAttributes(),
                attendee.getCreatedAt()
        );
    }

    private AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(attribute.getId(), attribute.getName(), attribute.getType(), attribute.getOptions());
    }
}
