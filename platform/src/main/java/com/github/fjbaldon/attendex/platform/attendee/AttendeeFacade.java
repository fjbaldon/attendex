package com.github.fjbaldon.attendex.platform.attendee;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendeeFacade {

    private final AttendeeIngestService ingestService;
    private final AttendeeQueryService queryService;
    private final AttendeeAttributeService attributeService;
    private final AttendeeImportService importService;

    // --- ATTENDEE INGEST ---

    @Transactional
    public AttendeeDto createAttendee(Long organizationId, CreateAttendeeDto dto) {
        return ingestService.createAttendee(organizationId, dto);
    }

    @Transactional
    public AttendeeDto updateAttendee(Long organizationId, Long attendeeId, UpdateAttendeeDto dto) {
        return ingestService.updateAttendee(organizationId, attendeeId, dto);
    }

    @Transactional
    public void deleteAttendee(Long organizationId, Long attendeeId) {
        ingestService.deleteAttendee(organizationId, attendeeId);
    }

    @Transactional
    public void deleteAttendees(Long organizationId, List<Long> attendeeIds) {
        ingestService.deleteAttendees(organizationId, attendeeIds);
    }

    // --- ATTENDEE QUERY ---

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findAttendees(Long organizationId, String query, Map<String, String> attributeFilters, Pageable pageable) {
        return queryService.findAttendees(organizationId, query, attributeFilters, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<AttendeeDto> findAttendeeById(Long attendeeId, Long organizationId) {
        return queryService.findAttendeeById(attendeeId, organizationId);
    }

    @Transactional(readOnly = true)
    public List<AttendeeDto> findAttendeesByIds(List<Long> attendeeIds) {
        return queryService.findAttendeesByIds(attendeeIds);
    }

    @Transactional(readOnly = true)
    public List<Long> findAttendeeIdsByFilters(Long organizationId, Map<String, String> filters) {
        return queryService.findAttendeeIdsByFilters(organizationId, filters);
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByCriteria(Long organizationId, String query, Map<String, String> attributeFilters) {
        return queryService.findIdsByCriteria(organizationId, query, attributeFilters);
    }

    // --- ATTRIBUTES ---

    @Transactional
    public AttributeDto createAttribute(Long organizationId, CreateAttributeDto dto) {
        return attributeService.createAttribute(organizationId, dto);
    }

    @Transactional(readOnly = true)
    public List<AttributeDto> findAttributes(Long organizationId) {
        return attributeService.findAttributes(organizationId);
    }

    @Transactional
    public AttributeDto updateAttribute(Long organizationId, Long attributeId, String newName, List<String> newOptions) {
        return attributeService.updateAttribute(organizationId, attributeId, newName, newOptions);
    }

    @Transactional
    public void deleteAttribute(Long organizationId, Long attributeId) {
        attributeService.deleteAttribute(organizationId, attributeId);
    }

    // --- IMPORT ---

    public List<String> extractCsvHeaders(MultipartFile file) throws IOException {
        return importService.extractCsvHeaders(file);
    }

    @Transactional(readOnly = true)
    public AttendeeImportAnalysisDto analyzeAttendeeImport(Long organizationId, MultipartFile file, ImportConfigurationDto config) throws IOException {
        return importService.analyzeAttendeeImport(organizationId, file, config);
    }

    @Transactional
    public void commitAttendeeImport(Long organizationId, List<CreateAttendeeDto> attendees, boolean updateExisting, List<String> newAttributes) {
        importService.commitAttendeeImport(organizationId, attendees, updateExisting, newAttributes);
    }

    @Transactional(readOnly = true)
    public String generateImportTemplate(Long organizationId) {
        return importService.generateImportTemplate(organizationId);
    }

    @Transactional(readOnly = true)
    public List<Long> findAttendeeIdsByAttributes(Long organizationId, Map<String, String> attributes) {
        return queryService.findIdsByCriteria(organizationId, null, attributes);
    }
}
