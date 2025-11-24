package com.github.fjbaldon.attendex.platform.event;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
class EventIngestService {

    private final EventRepository eventRepository;
    private final ApplicationEventPublisher eventPublisher;

    // REMOVED: CaptureFacade dependency
    EventIngestService(
            EventRepository eventRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EventDto createEvent(Long organizationId, Long organizerId, CreateEventRequestDto dto) {
        Event event = Event.create(
                organizationId,
                organizerId,
                dto.name(),
                dto.startDate(),
                dto.endDate(),
                dto.graceMinutesBefore(),
                dto.graceMinutesAfter()
        );

        dto.sessions().forEach(sessionDto -> {
            Session session = Session.create(sessionDto.activityName(), sessionDto.targetTime(), sessionDto.intent());
            event.addSession(session);
        });

        Event saved = eventRepository.save(event);
        eventPublisher.publishEvent(new EventCreatedEvent(saved.getId(), saved.getOrganizationId(), saved.getName()));

        return toDto(saved);
    }

    @Transactional
    public EventDto updateEvent(Long organizationId, Long eventId, UpdateEventRequestDto dto) {
        Event event = eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        int oldGraceBefore = event.getGraceMinutesBefore();
        int oldGraceAfter = event.getGraceMinutesAfter();

        event.updateDetails(
                dto.name(),
                dto.startDate(),
                dto.endDate(),
                dto.graceMinutesBefore(),
                dto.graceMinutesAfter()
        );

        Map<Long, UpdateSessionDto> incomingSessionsMap = dto.sessions().stream()
                .filter(s -> s.id() != null)
                .collect(Collectors.toMap(UpdateSessionDto::id, s -> s));

        List<SessionDetailsDto> remainingSessionTargets = dto.sessions().stream()
                .filter(s -> s.id() != null)
                .map(s -> new SessionDetailsDto(
                        s.id(),
                        s.targetTime(),
                        dto.graceMinutesBefore(),
                        dto.graceMinutesAfter()
                ))
                .collect(Collectors.toList());

        List<Session> existingSessions = new ArrayList<>(event.getSessions());

        for (Session session : existingSessions) {
            if (incomingSessionsMap.containsKey(session.getId())) {
                // --- UPDATE ---
                UpdateSessionDto updateDto = incomingSessionsMap.get(session.getId());

                boolean timeChanged = !session.getTargetTime().equals(updateDto.targetTime());
                boolean graceChanged = oldGraceBefore != dto.graceMinutesBefore() || oldGraceAfter != dto.graceMinutesAfter();

                session.update(updateDto.activityName(), updateDto.targetTime(), updateDto.intent());

                if (timeChanged || graceChanged) {
                    eventPublisher.publishEvent(new SessionUpdatedEvent(
                            session.getId(),
                            session.getTargetTime(),
                            event.getGraceMinutesBefore(),
                            event.getGraceMinutesAfter()
                    ));
                }
            } else {
                // --- DELETE ---
                eventPublisher.publishEvent(new SessionDeletedEvent(
                        session.getId(),
                        remainingSessionTargets
                ));

                event.removeSession(session);
            }
        }

        // --- CREATE ---
        dto.sessions().stream()
                .filter(s -> s.id() == null)
                .forEach(s -> {
                    Session newSession = Session.create(s.activityName(), s.targetTime(), s.intent());
                    event.addSession(newSession);
                });

        return toDto(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long organizationId, Long eventId) {
        Event event = eventRepository.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        event.markAsDeleted();
        eventRepository.save(event);
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
                event.calculateStatus(Instant.now()),
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
