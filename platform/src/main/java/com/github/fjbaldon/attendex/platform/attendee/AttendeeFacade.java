package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.attendee.dto.*;
import com.github.fjbaldon.attendex.platform.attendee.events.AttendeeCreatedEvent;
import com.github.fjbaldon.attendex.platform.attendee.events.AttendeeDeletedEvent;
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
                "An attendee with this identity already exists in your organization.");

        validateIdentityFormat(organizationId, dto.identity());

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
    public void deleteAttendee(Long organizationId, Long attendeeId) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .filter(a -> a.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Attendee not found"));

        attendeeRepository.delete(attendee);

        // NEW: Keep analytics in sync
        eventPublisher.publishEvent(new AttendeeDeletedEvent(organizationId));
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

        if (attributeRepository.isAttributeInUse(organizationId, attribute.getName())) {
            throw new IllegalStateException("Cannot delete attribute '" + attribute.getName() + "' because it is currently assigned to one or more attendees.");
        }

        attributeRepository.delete(attribute);
    }

    public List<String> extractCsvHeaders(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            CSVParser parser = csvFormat.parse(reader);
            return parser.getHeaderNames();
        }
    }

    @Transactional(readOnly = true)
    public AttendeeImportAnalysisDto analyzeAttendeeImport(Long organizationId, MultipartFile file, ImportConfigurationDto config) throws IOException {
        OrganizationDto organization = organizationFacade.findOrganizationById(organizationId);
        String regex = organization.identityFormatRegex();

        List<Attribute> existingAttributes = attributeRepository.findAllByOrganizationId(organizationId);
        Set<String> validAttributeNames = existingAttributes.stream().map(Attribute::getName).collect(Collectors.toSet());

        // Map existing options for validation (optional, if we want to allow expansion we skip strict check)
        // For this implementation, we allow expansion, so we don't fail if an option is new.

        List<CreateAttendeeDto> toCreate = new ArrayList<>();
        List<CreateAttendeeDto> toUpdate = new ArrayList<>();
        List<AttendeeImportAnalysisDto.InvalidRow> invalidRows = new ArrayList<>();
        Set<String> newAttributesToCreate = new HashSet<>();

        Set<String> fileIdentities = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).get();
            CSVParser parser = csvFormat.parse(reader);

            Map<String, String> mapping = config.columnMapping();

            for (CSVRecord record : parser) {
                try {
                    String identity = getMappedValue(record, mapping, "identity");
                    String firstName = getMappedValue(record, mapping, "firstName");
                    String lastName = getMappedValue(record, mapping, "lastName");

                    if (!StringUtils.hasText(identity)) throw new IllegalArgumentException("Row missing Identity.");
                    if (!StringUtils.hasText(firstName)) throw new IllegalArgumentException("Row missing First Name.");
                    if (!StringUtils.hasText(lastName)) throw new IllegalArgumentException("Row missing Last Name.");

                    if (regex != null && !regex.isBlank() && !identity.matches(regex)) {
                        throw new IllegalArgumentException("Identity format invalid.");
                    }

                    if (fileIdentities.contains(identity)) {
                        throw new IllegalArgumentException("Duplicate identity in file.");
                    }
                    fileIdentities.add(identity);

                    boolean existsInDb = attendeeRepository.existsByOrganizationIdAndIdentity(organizationId, identity);

                    if (existsInDb) {
                        if (config.mode() == ImportConfigurationDto.ImportMode.FAIL) {
                            throw new IllegalArgumentException("Identity already exists.");
                        }
                        if (config.mode() == ImportConfigurationDto.ImportMode.SKIP) {
                            continue;
                        }
                    }

                    Map<String, Object> attributeValues = new HashMap<>();
                    for (Map.Entry<String, String> entry : mapping.entrySet()) {
                        String csvHeader = entry.getKey();
                        String mappedTarget = entry.getValue();

                        if (List.of("identity", "firstName", "lastName").contains(mappedTarget)) continue;

                        String val = record.isMapped(csvHeader) ? record.get(csvHeader) : null;

                        if (StringUtils.hasText(val)) {
                            // DATA HYGIENE FIX: Enforce Upper Case
                            val = val.trim().toUpperCase();

                            if (validAttributeNames.contains(mappedTarget)) {
                                attributeValues.put(mappedTarget, val);
                            } else if (config.createMissingAttributes()) {
                                newAttributesToCreate.add(mappedTarget);
                                attributeValues.put(mappedTarget, val);
                            }
                        }
                    }

                    CreateAttendeeDto dto = new CreateAttendeeDto(identity, firstName, lastName, attributeValues);

                    if (existsInDb) {
                        toUpdate.add(dto);
                    } else {
                        toCreate.add(dto);
                    }

                } catch (Exception e) {
                    invalidRows.add(new AttendeeImportAnalysisDto.InvalidRow(record.getRecordNumber(), record.toMap(), e.getMessage()));
                }
            }
        }

        return new AttendeeImportAnalysisDto(toCreate, toUpdate, invalidRows, new ArrayList<>(newAttributesToCreate));
    }

    @Transactional
    public void commitAttendeeImport(Long organizationId, List<CreateAttendeeDto> attendees, boolean updateExisting, List<String> newAttributes) {
        // 1. Gather all unique options for every attribute in this batch
        Map<String, Set<String>> batchOptions = new HashMap<>();

        for (CreateAttendeeDto dto : attendees) {
            if (dto.attributes() != null) {
                dto.attributes().forEach((key, val) -> {
                    if (val instanceof String s && StringUtils.hasText(s)) {
                        batchOptions.computeIfAbsent(key, k -> new HashSet<>()).add(s);
                    }
                });
            }
        }

        // 2. Update or Create Attributes with new options
        // A. Handle NEW attributes
        if (newAttributes != null) {
            for (String attrName : newAttributes) {
                if (!attributeRepository.existsByOrganizationIdAndName(organizationId, attrName)) {
                    List<String> options = new ArrayList<>(batchOptions.getOrDefault(attrName, Collections.emptySet()));
                    Collections.sort(options); // Keep it tidy
                    Attribute attr = Attribute.create(organizationId, attrName, "SELECT", options);
                    attributeRepository.save(attr);
                }
            }
        }

        // B. Handle EXISTING attributes (Merge new options)
        List<Attribute> existingAttributes = attributeRepository.findAllByOrganizationId(organizationId);
        for (Attribute attr : existingAttributes) {
            if (batchOptions.containsKey(attr.getName())) {
                Set<String> newOptions = batchOptions.get(attr.getName());
                List<String> currentOptions = new ArrayList<>(attr.getOptions());
                boolean changed = false;

                for (String opt : newOptions) {
                    if (!currentOptions.contains(opt)) {
                        currentOptions.add(opt);
                        changed = true;
                    }
                }

                if (changed) {
                    Collections.sort(currentOptions);
                    attr.updateOptions(currentOptions);
                    attributeRepository.save(attr);
                }
            }
        }

        // 3. Process Attendees
        List<Attendee> batch = new ArrayList<>();
        for (CreateAttendeeDto dto : attendees) {
            Attendee existing = attendeeRepository.findAttendeeByIdentity(organizationId, dto.identity());

            if (existing != null && updateExisting) {
                existing.update(dto.firstName(), dto.lastName(), dto.attributes());
                batch.add(existing);
            } else if (existing == null) {
                Attendee newAttendee = Attendee.create(organizationId, dto.identity(), dto.firstName(), dto.lastName(), dto.attributes());
                batch.add(newAttendee);
                eventPublisher.publishEvent(new AttendeeCreatedEvent(newAttendee.getId(), organizationId));
            }
        }
        attendeeRepository.saveAll(batch);
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
            String sample = (attr.getOptions() != null && !attr.getOptions().isEmpty())
                    ? attr.getOptions().getFirst()
                    : "SampleValue";
            csv.append(",").append(sample);
        }
        csv.append("\n");
        return csv.toString();
    }

    private void validateIdentityFormat(Long organizationId, String identity) {
        OrganizationDto org = organizationFacade.findOrganizationById(organizationId);
        String regex = org.identityFormatRegex();
        if (regex != null && !regex.isBlank() && !identity.matches(regex)) {
            throw new IllegalArgumentException("Identity '" + identity + "' does not match the required format.");
        }
    }

    private String getMappedValue(CSVRecord record, Map<String, String> mapping, String targetField) {
        return mapping.entrySet().stream()
                .filter(e -> e.getValue().equals(targetField))
                .map(Map.Entry::getKey)
                .findFirst()
                .map(header -> record.isMapped(header) ? record.get(header) : null)
                .orElse(null);
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
