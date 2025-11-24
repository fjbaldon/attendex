package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;
import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.EntryEventStatusDto;
import com.github.fjbaldon.attendex.platform.capture.EventStatsDto;
import com.github.fjbaldon.attendex.platform.event.EventDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final SessionSummaryRepository sessionSummaryRepository;
    private final ScannerSummaryRepository scannerSummaryRepository;

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
        // 1. Get Event Totals
        EventSummary eventSummary = eventSummaryRepository.findById(eventId)
                .orElse(new EventSummary(eventId, organizationId, "Unknown"));

        // 2. Get Session Breakdowns
        List<SessionSummary> sessions = sessionSummaryRepository.findAllByEventId(eventId);
        Set<Long> sessionIds = sessions.stream().map(SessionSummary::getSessionId).collect(Collectors.toSet());
        Map<Long, String> sessionNames = eventFacade.getSessionNamesByIds(sessionIds);

        List<EventStatsDto.StatItem> sessionStats = sessions.stream()
                .map(s -> new EventStatsDto.StatItem(
                        sessionNames.getOrDefault(s.getSessionId(), "Unknown"),
                        s.getEntryCount()
                ))
                .collect(Collectors.toList());

        // 3. Get Scanner Breakdowns
        List<ScannerSummary> scanners = scannerSummaryRepository.findAllByEventId(eventId);
        Set<Long> scannerIds = scanners.stream().map(ScannerSummary::getScannerId).collect(Collectors.toSet());
        Map<Long, String> scannerEmails = organizationFacade.getScannerEmailsByIds(scannerIds);

        List<EventStatsDto.StatItem> scannerStats = scanners.stream()
                .map(s -> new EventStatsDto.StatItem(
                        scannerEmails.getOrDefault(s.getScannerId(), "Unknown"),
                        s.getEntryCount()
                ))
                .collect(Collectors.toList());

        // 4. Calculate Rate
        double rate = (eventSummary.getRosterCount() > 0)
                ? ((double) eventSummary.getEntryCount() / eventSummary.getRosterCount()) * 100.0
                : 0.0;

        Instant firstScan = captureFacade.getFirstScanTimestamp(eventId);
        Instant lastScan = captureFacade.getLastScanTimestamp(eventId);

        return new EventStatsDto(
                eventSummary.getEntryCount(),
                eventSummary.getRosterCount(),
                rate,
                firstScan, // Was null
                lastScan,  // Was null
                sessionStats,
                scannerStats
        );
    }

    // ... existing imports and class definition ...

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
        int realAbsentSessionsGlobal = 0; // FIXED: We count this manually now
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
                    // FIXED LOGIC:
                    // The "Absent" label should only appear when it is no longer possible to be "Late".
                    // The scanner accepts entries up to 4 hours (240 minutes) after the target time.
                    // We also clamp this to the Event End Date to avoid "Pending" showing after the event closes.

                    Instant sessionRelevanceEnd = session.targetTime().plus(4, java.time.temporal.ChronoUnit.HOURS);

                    // If the event ends *before* the 4 hour window, use the event end as the hard cutoff
                    if (event.endDate().isBefore(sessionRelevanceEnd)) {
                        sessionRelevanceEnd = event.endDate();
                    }

                    if (now.isAfter(sessionRelevanceEnd)) {
                        status = "ABSENT";
                        realAbsentSessionsGlobal++;
                    } else {
                        // If we are past the grace period but within the 4-hour window,
                        // they are "Pending" (implicitly Late if they arrive now).
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
                realAbsentSessionsGlobal, // FIXED: Pass the calculated absent count, NOT (total - attended)
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
