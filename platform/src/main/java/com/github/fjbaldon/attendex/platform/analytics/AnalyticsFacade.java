package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.capture.EntryEventStatusDto;
import com.github.fjbaldon.attendex.platform.capture.EventStatsDto;
import com.github.fjbaldon.attendex.platform.event.EventDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import jakarta.persistence.EntityNotFoundException;
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
public class AnalyticsFacade {

    private final AttributeBreakdownRepository attributeBreakdownRepository;
    private final OrganizationSummaryRepository organizationSummaryRepository;
    private final EventSummaryRepository eventSummaryRepository;

    private final CaptureFacade captureFacade;
    private final OrganizationFacade organizationFacade;
    private final AttendeeFacade attendeeFacade;
    private final EventFacade eventFacade;

    @Transactional(readOnly = true)
    public AttributeBreakdownDto getAttributeBreakdown(Long eventId, String attributeName) {
        List<AttributeBreakdown> results = attributeBreakdownRepository.findAllByEventIdAndAttributeName(eventId, attributeName);
        var items = results.stream()
                .map(item -> new AttributeBreakdownDto.BreakdownItem(item.getAttributeValue(), item.getAttendeeCount()))
                .collect(Collectors.toList());
        return new AttributeBreakdownDto(attributeName, items);
    }

    @Transactional(readOnly = true)
    public OrganizationSummaryDto getOrganizationSummary(Long organizationId) {
        return organizationSummaryRepository.findById(organizationId)
                .map(this::toDto)
                .orElse(new OrganizationSummaryDto(0, 0, 0));
    }

    @Transactional(readOnly = true)
    public Optional<EventSummaryDto> findEventSummary(Long eventId) {
        return eventSummaryRepository.findById(eventId).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long countTotalOrganizations() {
        return organizationSummaryRepository.count();
    }

    @Transactional(readOnly = true)
    public long countOrganizationsByLifecycle(String lifecycle) {
        return organizationFacade.countByLifecycle(lifecycle);
    }

    @Transactional(readOnly = true)
    public long countOrganizationsBySubscriptionType(String subscriptionType) {
        return organizationFacade.countBySubscriptionType(subscriptionType);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryDto> findRecentEventSummaries(Long organizationId, Pageable pageable) {
        return eventSummaryRepository.findByOrganizationId(organizationId, pageable).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventStatsDto getEventStats(Long organizationId, Long eventId) {
        return captureFacade.getEventStats(organizationId, eventId);
    }

    @Transactional(readOnly = true)
    public CohortStatsDto getCohortStats(Long organizationId, Long eventId, CohortStatsRequest request) {
        // 1. Get filtered attendee IDs based on attributes
        List<Long> attributeMatchingIds = attendeeFacade.findAttendeeIdsByAttributes(organizationId, request.filters());
        if (attributeMatchingIds.isEmpty()) return new CohortStatsDto(0, 0, 0, 0.0);

        // 2. Get Roster IDs for the event
        Set<Long> rosterIds = eventFacade.findAllAttendeeIdsForEvent(eventId);

        // 3. Intersect: Find attendees who match the profile AND are in the roster
        List<Long> actualCohortIds = attributeMatchingIds.stream()
                .filter(rosterIds::contains)
                .collect(Collectors.toList());

        if (actualCohortIds.isEmpty()) return new CohortStatsDto(0, 0, 0, 0.0);

        // 4. Count how many of these are present
        long present = captureFacade.countPresentFromList(eventId, request.sessionId(), actualCohortIds);
        long total = actualCohortIds.size();
        long absent = total - present;
        double rate = (double) present / total * 100.0;

        return new CohortStatsDto(total, present, absent, rate);
    }

    @Transactional(readOnly = true)
    public Page<CohortAttendeeDto> getCohortAttendees(Long organizationId, Long eventId, CohortStatsRequest request, Pageable pageable) {
        // 1. Get the Target Population (Cohort IDs)
        List<Long> attributeMatchingIds = attendeeFacade.findAttendeeIdsByAttributes(organizationId, request.filters());
        Set<Long> rosterIds = eventFacade.findAllAttendeeIdsForEvent(eventId);

        List<Long> cohortIds = attributeMatchingIds.stream()
                .filter(rosterIds::contains)
                .collect(Collectors.toList());

        if (cohortIds.isEmpty()) return Page.empty(pageable);

        // 2. Pagination in Memory (since ID list is filtered in memory)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), cohortIds.size());

        if (start > cohortIds.size()) return Page.empty(pageable);

        List<Long> pagedIds = cohortIds.subList(start, end);
        List<AttendeeDto> attendeeDetails = attendeeFacade.findAttendeesByIds(pagedIds);

        // 3. Check Presence
        List<EntryDetailsDto> entries;
        if (request.sessionId() != null) {
            // Find entries specifically for this session
            entries = captureFacade.findEntriesForSession(organizationId, request.sessionId(), pagedIds);
        } else {
            // Find entries for the whole event (sessionId = null, intent = null)
            // FIXED: Added 5th argument (null for intent)
            entries = captureFacade.findFilteredEntries(organizationId, eventId, null, null, pagedIds);
        }

        Map<Long, EntryDetailsDto> entryMap = entries.stream()
                .filter(e -> e.attendee() != null && e.attendee().id() != null)
                .collect(Collectors.toMap(e -> e.attendee().id(), Function.identity(), (e1, e2) -> e1));

        List<CohortAttendeeDto> dtos = attendeeDetails.stream().map(a -> {
            EntryDetailsDto entry = entryMap.get(a.id());
            boolean isPresent = entry != null;
            return new CohortAttendeeDto(
                    a.id(),
                    a.identity(),
                    a.firstName(),
                    a.lastName(),
                    isPresent ? "PRESENT" : "ABSENT",
                    isPresent ? entry.scanTimestamp() : null,
                    a.attributes()
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, cohortIds.size());
    }

    @Transactional(readOnly = true)
    public AttendeeHistoryDto getAttendeeHistory(Long organizationId, Long attendeeId) {
        AttendeeDto attendee = attendeeFacade.findAttendeeById(attendeeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attendee not found"));

        List<EventDto> events = eventFacade.findEventsForAttendee(attendeeId);
        List<EntryEventStatusDto> entries = captureFacade.getEntryStatusesForAttendee(attendeeId);

        Map<Long, EntryEventStatusDto> entryMap = entries.stream()
                .filter(e -> e.sessionId() != null)
                .collect(Collectors.toMap(EntryEventStatusDto::sessionId, Function.identity(), (e1, e2) -> e1));

        List<AttendeeHistoryItemDto> history = new ArrayList<>();
        int totalSessionsGlobal = 0;
        int attendedSessionsGlobal = 0;
        int realAbsentSessionsGlobal = 0;
        Instant now = Instant.now();

        for (EventDto event : events) {
            List<SessionHistoryItemDto> sessionHistory = new ArrayList<>();
            int eventSessionsCompleted = 0;

            for (var session : event.sessions()) {
                EntryEventStatusDto entry = entryMap.get(session.id());
                String status;
                Instant scanTime = null;

                if (entry != null) {
                    status = "PUNCTUAL".equals(entry.punctuality()) ? "PRESENT" : entry.punctuality();
                    scanTime = entry.scanTimestamp();
                    eventSessionsCompleted++;
                    attendedSessionsGlobal++;
                } else {
                    Instant sessionRelevanceEnd = session.targetTime().plus(4, java.time.temporal.ChronoUnit.HOURS);
                    if (event.endDate().isBefore(sessionRelevanceEnd)) {
                        sessionRelevanceEnd = event.endDate();
                    }

                    if (now.isAfter(sessionRelevanceEnd)) {
                        status = "ABSENT";
                        realAbsentSessionsGlobal++;
                    } else {
                        status = "PENDING";
                    }
                }

                totalSessionsGlobal++;

                sessionHistory.add(new SessionHistoryItemDto(
                        session.id(),
                        session.activityName(),
                        session.intent(),
                        session.targetTime(),
                        status,
                        scanTime
                ));
            }

            sessionHistory.sort(Comparator.comparing(SessionHistoryItemDto::targetTime));

            history.add(new AttendeeHistoryItemDto(
                    event.id(),
                    event.name(),
                    event.startDate(),
                    eventSessionsCompleted,
                    event.sessions().size(),
                    sessionHistory
            ));
        }

        double rate = (totalSessionsGlobal == 0) ? 0.0 : ((double) attendedSessionsGlobal / totalSessionsGlobal) * 100;

        return new AttendeeHistoryDto(
                attendee,
                events.size(),
                attendedSessionsGlobal,
                realAbsentSessionsGlobal,
                rate,
                history
        );
    }

    private OrganizationSummaryDto toDto(OrganizationSummary summary) {
        return new OrganizationSummaryDto(
                summary.getTotalEvents(),
                summary.getTotalAttendees(),
                summary.getTotalScanners()
        );
    }

    private EventSummaryDto toDto(EventSummary summary) {
        double attendanceRate = (summary.getRosterCount() > 0)
                ? ((double) summary.getEntryCount() / summary.getRosterCount()) * 100.0
                : 0.0;

        return new EventSummaryDto(
                summary.getEventId(),
                summary.getOrganizationId(),
                summary.getRosterCount(),
                summary.getEntryCount(),
                summary.getEventName(),
                attendanceRate
        );
    }
}
