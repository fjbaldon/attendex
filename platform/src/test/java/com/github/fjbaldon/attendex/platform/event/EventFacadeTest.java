package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.event.dto.CreateEventRequestDto;
import com.github.fjbaldon.attendex.platform.event.dto.SessionDto;
import com.github.fjbaldon.attendex.platform.event.events.EventCreatedEvent;
import com.github.fjbaldon.attendex.platform.event.events.RosterEntryAddedEvent;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventFacadeTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private RosterRepository rosterRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private AttendeeFacade attendeeFacade; // Mocking the other module's Facade
    @Mock
    private CaptureFacade captureFacade;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EventFacade eventFacade;

    @Test
    void createEvent_shouldPersistAndPublishEvent() {
        // Given
        var sessionDto = new SessionDto("Session A", Instant.now(), "Arrival");
        var request = new CreateEventRequestDto(
                "My Event",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                15, 15,
                List.of(sessionDto)
        );

        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            // Simulate DB ID assignment
            var idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(e, 100L);
            return e;
        });

        // When
        eventFacade.createEvent(1L, 1L, request);

        // Then
        verify(eventRepository).save(any(Event.class));
        verify(eventPublisher).publishEvent(any(EventCreatedEvent.class));
    }

    @Test
    void addAttendeeToRoster_shouldSucceed_whenAttendeeExists() throws Exception {
        // Given
        Long eventId = 100L;
        Long attendeeId = 500L;
        Long orgId = 1L;

        // Mock Attendee existence (Cross-module call)
        when(attendeeFacade.findAttendeeById(attendeeId, orgId))
                .thenReturn(Optional.of(new AttendeeDto(attendeeId, "ID", "John", "Doe", null, Instant.now())));

        // Mock Event existence
        // We need to create a dummy Event entity to return
        var eventConstructor = Event.class.getDeclaredConstructor(Long.class, Long.class, String.class, Instant.class, Instant.class, int.class, int.class);
        eventConstructor.setAccessible(true);
        Event mockEvent = eventConstructor.newInstance(orgId, 1L, "Test Event", Instant.now(), Instant.now(), 15, 15);

        // Set ID via reflection
        var idField = Event.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockEvent, eventId);

        when(eventRepository.findByIdAndOrganizationId(eventId, orgId)).thenReturn(Optional.of(mockEvent));
        when(rosterRepository.existsById(new RosterEntryId(eventId, attendeeId))).thenReturn(false);

        // When
        eventFacade.addAttendeeToRoster(eventId, attendeeId, orgId);

        // Then
        verify(rosterRepository).save(any(RosterEntry.class));
        verify(eventPublisher).publishEvent(any(RosterEntryAddedEvent.class));
    }

    @Test
    void addAttendeeToRoster_shouldFail_whenAttendeeDoesNotExist() {
        // Given
        Long eventId = 100L;
        Long attendeeId = 999L;
        Long orgId = 1L;

        when(attendeeFacade.findAttendeeById(attendeeId, orgId)).thenReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> eventFacade.addAttendeeToRoster(eventId, attendeeId, orgId));

        // Then
        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Attendee not found in this organization.");

        verify(rosterRepository, never()).save(any());
    }
}
