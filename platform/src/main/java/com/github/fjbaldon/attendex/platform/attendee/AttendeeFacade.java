package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.attendee.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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

    @Transactional
    public AttendeeDto createAttendee(Long organizationId, CreateAttendeeDto dto) {
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
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findAttendees(Long organizationId, Pageable pageable) {
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

    @Transactional(readOnly = true)
    public AttendeeImportAnalysisDto analyzeAttendeeImport(Long organizationId, MultipartFile file) throws IOException {
        List<Attribute> attributes = attributeRepository.findAllByOrganizationId(organizationId);
        Map<String, Attribute> attributeMap = attributes.stream()
                .collect(Collectors.toMap(attr -> attr.getName().toLowerCase(), Function.identity()));

        List<CreateAttendeeDto> validAttendees = new ArrayList<>();
        List<AttendeeImportAnalysisDto.InvalidRow> invalidRows = new ArrayList<>();
        Set<String> identifiersInThisFile = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get();
            CSVParser csvParser = CSVParser.parse(reader, csvFormat);

            for (CSVRecord record : csvParser) {
                try {
                    String identity = record.get("identity");
                    Assert.hasText(identity, "Identity is a required field.");

                    if (attendeeRepository.existsByOrganizationIdAndIdentity(organizationId, identity) || identifiersInThisFile.contains(identity)) {
                        throw new IllegalStateException("Identity '" + identity + "' already exists or is duplicated in the file.");
                    }
                    identifiersInThisFile.add(identity);

                    Map<String, Object> attributeValues = new HashMap<>();
                    for (String header : csvParser.getHeaderNames()) {
                        Attribute attribute = attributeMap.get(header.toLowerCase());
                        if (attribute != null && record.isMapped(header) && StringUtils.hasText(record.get(header))) {
                            String value = record.get(header);
                            Assert.isTrue(attribute.getOptions().contains(value),
                                    "Value '" + value + "' is not a valid option for attribute '" + attribute.getName() + "'.");
                            attributeValues.put(attribute.getName(), value);
                        }
                    }

                    validAttendees.add(new CreateAttendeeDto(
                            identity,
                            record.get("firstName"),
                            record.get("lastName"),
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
