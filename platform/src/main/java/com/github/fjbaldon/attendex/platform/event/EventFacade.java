package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EventFacade {

    private final EventIngestService ingestService;
    private final EventQueryService queryService;
    private final RosterService rosterService;
    private final EventSyncService syncService;

    EventFacade(
            EventIngestService ingestService,
            EventQueryService queryService,
            RosterService rosterService,
            EventSyncService syncService
    ) {
        this.ingestService = ingestService;
        this.queryService = queryService;
        this.rosterService = rosterService;
        this.syncService = syncService;
    }

    @Transactional
    public EventDto createEvent(Long organizationId, Long organizerId, CreateEventRequestDto dto) {
        return ingestService.createEvent(organizationId, organizerId, dto);
    }

    @Transactional
    public EventDto updateEvent(Long organizationId, Long eventId, UpdateEventRequestDto dto) {
        return ingestService.updateEvent(organizationId, eventId, dto);
    }

    @Transactional
    public void deleteEvent(Long organizationId, Long eventId) {
        ingestService.deleteEvent(organizationId, eventId);
    }

    // --- QUERY ---

    @Transactional(readOnly = true)
    public Page<EventDto> findEvents(Long organizationId, String query, Pageable pageable) {
        return queryService.findEvents(organizationId, query, pageable);
    }

    @Transactional(readOnly = true)
    public EventDto findEventById(Long eventId, Long organizationId) {
        return queryService.findEventById(eventId, organizationId);
    }

    @Transactional(readOnly = true)
    public Page<EventDto> findUpcomingEvents(Long organizationId, Pageable pageable) {
        return queryService.findUpcomingEvents(organizationId, pageable);
    }

    @Transactional(readOnly = true)
    public List<SessionDetailsDto> findAllSessionsForEvent(Long eventId) {
        return queryService.findAllSessionsForEvent(eventId);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getSessionNamesByIds(Set<Long> sessionIds) {
        return queryService.getSessionNamesByIds(sessionIds);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getEventNamesByIds(Set<Long> eventIds) {
        return queryService.getEventNamesByIds(eventIds);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long eventId) {
        return queryService.exists(eventId);
    }

    @Transactional(readOnly = true)
    public void ensureEventExists(Long eventId, Long organizationId) {
        queryService.ensureEventExists(eventId, organizationId);
    }

    // --- ROSTER ---

    @Transactional
    public void addAttendeeToRoster(Long eventId, Long attendeeId, Long organizationId) {
        rosterService.addAttendeeToRoster(eventId, attendeeId, organizationId);
    }

    @Transactional
    public void removeAttendeeFromRoster(Long eventId, Long attendeeId) {
        rosterService.removeAttendeeFromRoster(eventId, attendeeId);
    }

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findRosterForEvent(Long eventId, Long organizationId, String query, Map<String, String> attributeFilters, Pageable pageable) {
        return rosterService.findRosterForEvent(eventId, organizationId, query, attributeFilters, pageable);
    }

    @Transactional(readOnly = true)
    public long countRosterForEvent(Long eventId) {
        return rosterService.countRosterForEvent(eventId);
    }

    @Transactional
    public int bulkAddAttendeesByCriteria(Long eventId, Long organizationId, BulkAddCriteriaRequestDto criteria) {
        return rosterService.bulkAddAttendeesByCriteria(eventId, organizationId, criteria);
    }

    @Transactional(readOnly = true)
    public List<EventDto> findEventsForAttendee(Long attendeeId) {
        return rosterService.findEventsForAttendee(attendeeId);
    }

    // --- SYNC ---

    @Transactional(readOnly = true)
    public List<EventSyncDto> getEventsForSync(Long organizationId) {
        return syncService.getEventsForSync(organizationId);
    }

    @Transactional(readOnly = true)
    public Page<EventSyncDto.RosterSyncDto> getFormattedRosterForSync(Long eventId, Long organizationId, Pageable pageable) {
        return syncService.getFormattedRosterForSync(eventId, organizationId, pageable);
    }

    @Transactional(readOnly = true)
    public Set<Long> findAllAttendeeIdsForEvent(Long eventId) {
        return rosterService.findAllAttendeeIdsForEvent(eventId);
    }
}
