package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.event.SessionDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptureFacade {

    private final CaptureIngestService ingestService;
    private final CaptureQueryService queryService;
    private final OrphanService orphanService;

    // --- INGESTION DELEGATES ---

    @Transactional
    public BatchSyncResponse syncEntries(Long organizationId, String scannerEmail, EntrySyncRequestDto request) {
        return ingestService.syncEntries(organizationId, scannerEmail, request);
    }

    @Transactional
    public void recoverOrphan(Long organizationId, Long orphanId, Long targetEventId, Long actorId) {
        EntrySyncRequestDto.EntryRecord originalRecord = orphanService.getOrphanPayloadForRecovery(organizationId, orphanId);

        EntrySyncRequestDto.EntryRecord newRecord = new EntrySyncRequestDto.EntryRecord(
                originalRecord.scanUuid(),
                targetEventId,
                originalRecord.attendeeId(),
                originalRecord.scanTimestamp(),
                originalRecord.snapshotIdentity(),
                originalRecord.snapshotFirstName(),
                originalRecord.snapshotLastName()
        );

        boolean saved = ingestService.processSingleEntry(organizationId, actorId, newRecord, new HashMap<>());

        if (saved) {
            orphanService.deleteOrphanAfterRecovery(organizationId, orphanId);
            log.info("Recovered orphan {} to event {}", orphanId, targetEventId);
        } else {
            throw new IllegalStateException("Entry was skipped (duplicate). Orphan deleted.");
        }
    }

    @Transactional
    public void recalculateSessionPunctuality(Long sessionId, Instant targetTime, int graceBefore, int graceAfter) {
        ingestService.recalculateSessionPunctuality(sessionId, targetTime, graceBefore, graceAfter);
    }

    // --- QUERY DELEGATES ---

    @Transactional(readOnly = true)
    public List<EntryDetailsDto> findFilteredEntries(Long organizationId, Long eventId, String intent, List<Long> attendeeIds) {
        return queryService.findFilteredEntries(organizationId, eventId, intent, attendeeIds);
    }

    @Transactional(readOnly = true)
    public Page<EntryDetailsDto> findFilteredEntriesPaginated(Long eventId, Long organizationId, String intent, String query, Pageable pageable) {
        return queryService.findEntriesByEventAndIntent(eventId, organizationId, intent, query, pageable);
    }

    @Transactional(readOnly = true)
    public long countEntriesSince(Long organizationId, Instant timestamp) {
        return queryService.countEntriesSince(organizationId, timestamp);
    }

    @Transactional(readOnly = true)
    public EventStatsDto getEventStats(Long organizationId, Long eventId) {
        return queryService.getEventStats(organizationId, eventId);
    }

    @Transactional(readOnly = true)
    public List<RecentActivityDto> getRecentActivity(Long organizationId) {
        return queryService.getRecentActivity(organizationId);
    }

    @Transactional(readOnly = true)
    public List<DailyEntryCount> getDailyActivity(Long organizationId, Instant startDate) {
        return queryService.getDailyActivity(organizationId, startDate);
    }

    @Transactional(readOnly = true)
    public List<EntryEventStatusDto> getEntryStatusesForAttendee(Long attendeeId) {
        return queryService.getEntryStatusesForAttendee(attendeeId);
    }

    // --- ORPHAN DELEGATES ---

    @Transactional(readOnly = true)
    public Page<OrphanedEntryDto> getOrphanedEntries(Long organizationId, Pageable pageable) {
        return orphanService.getOrphanedEntries(organizationId, pageable);
    }

    @Transactional
    public void deleteOrphanedEntry(Long organizationId, Long orphanId) {
        orphanService.deleteOrphanedEntry(organizationId, orphanId);
    }

    @Transactional
    public void reassignEntriesForDeletedSession(Long sessionIdToDelete, List<SessionDetailsDto> remainingSessions) {
        ingestService.reassignEntriesForDeletedSession(sessionIdToDelete, remainingSessions);
    }

    @Transactional(readOnly = true)
    public Instant getFirstScanTimestamp(Long eventId) {
        return queryService.findFirstScanTimestamp(eventId);
    }

    @Transactional(readOnly = true)
    public Instant getLastScanTimestamp(Long eventId) {
        return queryService.findLastScanTimestamp(eventId);
    }
}
