package com.github.fjbaldon.attendex.platform.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class OrphanService {

    private final OrphanedEntryRepository orphanedEntryRepository;
    private final EventFacade eventFacade;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<OrphanedEntryDto> getOrphanedEntries(Long organizationId, Pageable pageable) {
        Page<OrphanedEntry> page = orphanedEntryRepository.findAllByOrganizationId(organizationId, pageable);

        Set<Long> eventIds = page.getContent().stream()
                .map(OrphanedEntry::getOriginalEventId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> eventNames = eventFacade.getEventNamesByIds(eventIds);

        return page.map(entity -> new OrphanedEntryDto(
                entity.getId(),
                entity.getOriginalEventId(),
                eventNames.getOrDefault(entity.getOriginalEventId(), "Unknown/Deleted Event"),
                entity.getScanUuid(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getRawPayload()
        ));
    }

    @Transactional
    public void deleteOrphanedEntry(Long organizationId, Long orphanId) {
        OrphanedEntry orphan = orphanedEntryRepository.findByIdAndOrganizationId(orphanId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Orphaned entry not found"));
        orphanedEntryRepository.delete(orphan);
    }

    @Transactional
    public EntrySyncRequestDto.EntryRecord getOrphanPayloadForRecovery(Long organizationId, Long orphanId) {
        OrphanedEntry orphan = orphanedEntryRepository.findByIdAndOrganizationId(orphanId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Orphaned entry not found"));

        try {
            return objectMapper.convertValue(orphan.getRawPayload(), EntrySyncRequestDto.EntryRecord.class);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to parse orphan payload", e);
        }
    }

    @Transactional
    public void deleteOrphanAfterRecovery(Long organizationId, Long orphanId) {
        // Verify exists before deleting
        OrphanedEntry orphan = orphanedEntryRepository.findByIdAndOrganizationId(orphanId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Orphaned entry not found"));
        orphanedEntryRepository.delete(orphan);
    }

    @Transactional
    public void saveToQuarantine(Long organizationId, EntrySyncRequestDto.EntryRecord record, String reason) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadMap = objectMapper.convertValue(record, Map.class);

            OrphanedEntry orphan = OrphanedEntry.create(
                    organizationId,
                    record.eventId(),
                    record.scanUuid(),
                    payloadMap,
                    reason
            );
            orphanedEntryRepository.save(orphan);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert orphan payload for UUID {}", record.scanUuid(), e);
        }
    }
}
