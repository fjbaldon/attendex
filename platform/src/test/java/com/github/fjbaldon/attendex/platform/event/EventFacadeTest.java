package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.event.dto.CreateEventRequestDto;
import com.github.fjbaldon.attendex.platform.event.dto.EventDto;
import com.github.fjbaldon.attendex.platform.event.dto.SessionDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

class EventFacadeTest {

    private EventFacade eventFacade;
    private AttendeeFacade attendeeFacade; // Mocked external dependency
    private CaptureFacade captureFacade;   // Mocked external dependency
    private RosterRepository rosterRepository;

    @BeforeEach
    void setUp() {
        // Mocks for dependencies from other modules
        attendeeFacade = Mockito.mock(AttendeeFacade.class);
        captureFacade = Mockito.mock(CaptureFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        // Real in-memory fakes for this module's repositories
        EventRepository eventRepository = new InMemoryEventRepository();
        rosterRepository = new InMemoryRosterRepository();
        SessionRepository sessionRepository = new InMemorySessionRepository();

        // Construct the facade with the correct dependencies in the correct order
        eventFacade = new EventFacade(
                eventRepository,
                rosterRepository,
                sessionRepository,
                attendeeFacade,
                captureFacade,
                eventPublisher
        );
    }

    private EventDto createTestEvent() {
        var sessionDto = new SessionDto("Test Session", Instant.now(), "Arrival");
        var eventRequest = new CreateEventRequestDto(
                "Test Event",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                15,
                15,
                List.of(sessionDto)
        );
        return eventFacade.createEvent(1L, 1L, eventRequest);
    }

    @Test
    void shouldAddAttendeeToRosterWhenAttendeeExists() {
        // Given
        EventDto event = createTestEvent();
        AttendeeDto attendee = new AttendeeDto(100L, "ID123", "John", "Doe", null, Instant.now());
        when(attendeeFacade.findAttendeeById(100L, 1L)).thenReturn(Optional.of(attendee));

        // When
        eventFacade.addAttendeeToRoster(event.id(), 100L, 1L);

        // Then
        assertThat(rosterRepository.count()).isEqualTo(1);
        RosterEntry savedEntry = rosterRepository.findAll().iterator().next();
        assertThat(savedEntry.getId().getAttendeeId()).isEqualTo(100L);
        assertThat(savedEntry.getId().getEventId()).isEqualTo(event.id());
    }

    @Test
    void shouldFailToAddAttendeeToRosterWhenAttendeeDoesNotExist() {
        // Given
        EventDto event = createTestEvent();
        when(attendeeFacade.findAttendeeById(999L, 1L)).thenReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> eventFacade.addAttendeeToRoster(event.id(), 999L, 1L));

        // Then
        assertThat(thrown).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Attendee not found in this organization.");
        assertThat(rosterRepository.count()).isZero();
    }
}
