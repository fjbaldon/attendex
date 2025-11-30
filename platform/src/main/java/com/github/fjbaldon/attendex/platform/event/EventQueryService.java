package com.github.fjbaldon.attendex.platform.event;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class EventQueryService {

    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public Page<EventDto> findEvents(Long organizationId, String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return eventRepository.searchByOrganizationId(organizationId, query.trim(), pageable)
                    .map(this::toDto);
        }
        return eventRepository.findAllByOrganizationId(organizationId, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public EventDto findEventById(Long eventId, Long organizationId) {
        return eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
    }

    @Transactional(readOnly = true)
    public Page<EventDto> findUpcomingEvents(Long organizationId, Pageable pageable) {
        return eventRepository.findByOrganizationIdAndStartDateAfterOrderByStartDateAsc(
                organizationId,
                Instant.now(),
                pageable
        ).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<SessionDetailsDto> findAllSessionsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return event.getSessions().stream()
                .map(s -> new SessionDetailsDto(
                        s.getId(),
                        s.getTargetTime(),
                        event.getGraceMinutesBefore(),
                        event.getGraceMinutesAfter()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getSessionNamesByIds(Set<Long> sessionIds) {
        return sessionRepository.findAllById(sessionIds).stream()
                .collect(Collectors.toMap(Session::getId, Session::getActivityName));
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getEventNamesByIds(Set<Long> eventIds) {
        return eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, Event::getName));
    }

    @Transactional(readOnly = true)
    public boolean exists(Long eventId) {
        return eventRepository.existsByIdAndDeletedAtIsNull(eventId);
    }

    @Transactional(readOnly = true)
    public void ensureEventExists(Long eventId, Long organizationId) {
        if (eventRepository.findByIdAndOrganizationId(eventId, organizationId).isEmpty()) {
            throw new EntityNotFoundException("Event not found");
        }
    }

    private EventDto toDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getStartDate(),
                event.getEndDate(),
                event.getGraceMinutesBefore(),
                event.getGraceMinutesAfter(),
                event.getCreatedAt(),
                event.calculateStatus(Instant.now()), // Use entity method
                event.getSessions().stream().map(this::toSessionDto).collect(Collectors.toList())
        );
    }

    private SessionDto toSessionDto(Session session) {
        return new SessionDto(
                session.getId(),
                session.getActivityName(),
                session.getTargetTime(),
                session.getIntent()
        );
    }
}
