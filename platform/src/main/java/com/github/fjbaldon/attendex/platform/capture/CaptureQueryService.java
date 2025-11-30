package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
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
    public Page<EntryDetailsDto> findEntries(Long eventId, Long organizationId, Long sessionId, String intent, String query, Map<String, String> attributeFilters, Pageable pageable) {
        eventFacade.ensureEventExists(eventId, organizationId);

        List<Long> attendeeIds = Collections.emptyList();
        boolean hasAttendeeFilter = false;

        if (attributeFilters != null && !attributeFilters.isEmpty()) {
            attendeeIds = attendeeFacade.findAttendeeIdsByFilters(organizationId, attributeFilters);
            hasAttendeeFilter = true;
            if (attendeeIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        Page<Entry> entriesPage = entryRepository.findAllEntries(eventId, sessionId, intent, query, attendeeIds, hasAttendeeFilter, pageable);

        List<EntryDetailsDto> dtos = mapEntriesToDtos(entriesPage.getContent());
        return new PageImpl<>(dtos, pageable, entriesPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<EntryDetailsDto> findFilteredEntries(Long organizationId, Long eventId, Long sessionId, String intent, List<Long> attendeeIds) {
        eventFacade.findEventById(eventId, organizationId);

        if (attendeeIds != null && attendeeIds.isEmpty()) {
            return Collections.emptyList();
        }

        boolean hasAttendeeFilter = (attendeeIds != null);
        List<Long> safeIds = hasAttendeeFilter ? attendeeIds : Collections.emptyList();

        List<Entry> entries = entryRepository.findEntriesForReport(eventId, sessionId, intent, safeIds, hasAttendeeFilter);
        return mapEntriesToDtos(entries);
    }

    @Transactional(readOnly = true)
    public List<EntryDetailsDto> findEntriesForSession(Long organizationId, Long sessionId, List<Long> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return mapEntriesToDtos(entryRepository.findByOrganizationIdAndSessionIdAndAttendeeIdIn(organizationId, sessionId, attendeeIds));
    }

    @Transactional(readOnly = true)
    public long countPresentAttendeesInList(Long eventId, Long sessionId, List<Long> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) return 0;

        if (sessionId != null) {
            return entryRepository.countPresentForSession(sessionId, attendeeIds);
        } else {
            return entryRepository.countPresentEventWide(eventId, attendeeIds);
        }
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

                    // inside mapEntriesToDtos
                    return new EntryDetailsDto(
                            entry.getId(),
                            entry.getSessionId(), // Map sessionId
                            entry.getScanTimestamp(),
                            entry.getPunctuality(),
                            displayAttendee
                    );
                })
                .collect(Collectors.toList());
    }
}
