package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class CaptureQueryService {

    private final EntryRepository entryRepository;
    private final EventFacade eventFacade;
    private final AttendeeFacade attendeeFacade;
    private final OrganizationFacade organizationFacade;

    @Transactional(readOnly = true)
    public List<EntryDetailsDto> findFilteredEntries(Long organizationId, Long eventId, String intent, List<Long> attendeeIds) {
        eventFacade.findEventById(eventId, organizationId);
        List<Entry> entries = entryRepository.findEntriesForReport(eventId, intent, attendeeIds);
        return mapEntriesToDtos(entries);
    }

    @Transactional(readOnly = true)
    public Page<EntryDetailsDto> findEntriesByEventAndIntent(Long eventId, Long organizationId, String intent, String query, Pageable pageable) {
        eventFacade.ensureEventExists(eventId, organizationId);
        List<Long> sessionIds = eventFacade.findSessionIdsByIntent(eventId, intent);

        if (sessionIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Entry> entriesPage = entryRepository.findBySessionIdsAndQuery(sessionIds, query, pageable);
        List<EntryDetailsDto> dtos = mapEntriesToDtos(entriesPage.getContent());
        return new PageImpl<>(dtos, pageable, entriesPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public long countEntriesSince(Long organizationId, Instant timestamp) {
        return entryRepository.countByOrganizationIdAndSyncTimestampAfter(organizationId, timestamp);
    }

    @Transactional(readOnly = true)
    public List<DailyEntryCount> getDailyActivity(Long organizationId, Instant startDate) {
        return entryRepository.findDailyEntriesSince(organizationId, startDate);
    }

    @Transactional(readOnly = true)
    public List<EntryEventStatusDto> getEntryStatusesForAttendee(Long attendeeId) {
        return entryRepository.findByAttendeeId(attendeeId).stream()
                .map(e -> new EntryEventStatusDto(
                        e.getEventId(),
                        e.getSessionId(),
                        e.getScanTimestamp(),
                        e.getPunctuality()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventStatsDto getEventStats(Long organizationId, Long eventId) {
        eventFacade.findEventById(eventId, organizationId);
        long totalScans = entryRepository.countByEventId(eventId);
        long rosterCount = eventFacade.countRosterForEvent(eventId);
        double rate = (rosterCount > 0) ? ((double) totalScans / rosterCount) * 100 : 0.0;

        List<Object[]> sessionCounts = entryRepository.countByEventIdGroupBySessionId(eventId);
        Set<Long> sessionIds = sessionCounts.stream().map(row -> (Long) row[0]).collect(Collectors.toSet());
        Map<Long, String> sessionNames = eventFacade.getSessionNamesByIds(sessionIds);

        List<EventStatsDto.StatItem> sessionStats = sessionCounts.stream()
                .map(row -> new EventStatsDto.StatItem(
                        sessionNames.getOrDefault((Long) row[0], "Unknown"),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());

        List<Object[]> scannerCounts = entryRepository.countByEventIdGroupByScannerId(eventId);
        Set<Long> scannerIds = scannerCounts.stream().map(row -> (Long) row[0]).collect(Collectors.toSet());
        Map<Long, String> scannerEmails = organizationFacade.getScannerEmailsByIds(scannerIds);

        List<EventStatsDto.StatItem> scannerStats = scannerCounts.stream()
                .map(row -> new EventStatsDto.StatItem(
                        scannerEmails.getOrDefault((Long) row[0], "Unknown"),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());

        Instant first = entryRepository.findFirstScanByEventId(eventId).orElse(null);
        Instant last = entryRepository.findLastScanByEventId(eventId).orElse(null);

        return new EventStatsDto(totalScans, rosterCount, rate, first, last, sessionStats, scannerStats);
    }

    @Transactional(readOnly = true)
    public List<RecentActivityDto> getRecentActivity(Long organizationId) {
        List<Entry> entries = entryRepository.findTop5ByOrganizationIdOrderByScanTimestampDesc(organizationId);
        List<EntryDetailsDto> dtos = mapEntriesToDtos(entries);
        Set<Long> eventIds = entries.stream().map(Entry::getEventId).collect(Collectors.toSet());
        Map<Long, String> eventNames = eventFacade.getEventNamesByIds(eventIds);

        return dtos.stream().map(dto -> {
            Entry original = entries.stream().filter(e -> e.getId().equals(dto.entryId())).findFirst().orElseThrow();
            String eventName = eventNames.getOrDefault(original.getEventId(), "Unknown");
            return new RecentActivityDto(dto.attendee().firstName() + " " + dto.attendee().lastName(), eventName, dto.scanTimestamp(), dto.punctuality());
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Instant findFirstScanTimestamp(Long eventId) {
        return entryRepository.findFirstScanByEventId(eventId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Instant findLastScanTimestamp(Long eventId) {
        return entryRepository.findLastScanByEventId(eventId).orElse(null);
    }

    private List<EntryDetailsDto> mapEntriesToDtos(List<Entry> entries) {
        if (entries.isEmpty()) return Collections.emptyList();

        List<Long> idsNeedingFallback = entries.stream()
                .filter(e -> e.getSnapshotIdentity() == null || e.getSnapshotAttributes() == null)
                .map(Entry::getAttendeeId)
                .distinct()
                .toList();

        Map<Long, AttendeeDto> liveFallbackMap = new HashMap<>();
        if (!idsNeedingFallback.isEmpty()) {
            liveFallbackMap = attendeeFacade.findAttendeesByIds(idsNeedingFallback)
                    .stream()
                    .collect(Collectors.toMap(AttendeeDto::id, Function.identity()));
        }

        Map<Long, AttendeeDto> finalLiveFallbackMap = liveFallbackMap;

        return entries.stream()
                .map(entry -> {
                    String identity = entry.getSnapshotIdentity();
                    String firstName = entry.getSnapshotFirstName();
                    String lastName = entry.getSnapshotLastName();
                    Map<String, Object> attributes = entry.getSnapshotAttributes();

                    if (identity == null || attributes == null) {
                        AttendeeDto live = finalLiveFallbackMap.get(entry.getAttendeeId());
                        if (live != null) {
                            if (identity == null) {
                                identity = live.identity();
                                firstName = live.firstName();
                                lastName = live.lastName();
                            }
                            if (attributes == null) {
                                attributes = live.attributes();
                            }
                        } else if (identity == null) {
                            identity = "UNKNOWN";
                            firstName = "Deleted";
                            lastName = "Attendee";
                        }
                    }

                    if (attributes == null) attributes = Collections.emptyMap();

                    AttendeeDto displayAttendee = new AttendeeDto(
                            entry.getAttendeeId(), identity, firstName, lastName, attributes, null
                    );

                    return new EntryDetailsDto(
                            entry.getId(), entry.getScanTimestamp(), entry.getPunctuality(), displayAttendee
                    );
                })
                .collect(Collectors.toList());
    }
}
