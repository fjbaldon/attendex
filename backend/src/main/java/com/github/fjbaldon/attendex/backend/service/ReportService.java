package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.EventAnalyticsResponse;
import com.github.fjbaldon.attendex.backend.model.Event;
import com.github.fjbaldon.attendex.backend.repository.AttendanceRecordRepository;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EventRepository eventRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @Transactional(readOnly = true)
    public EventAnalyticsResponse getEventAnalytics(Long eventId, String organizerUsername) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (!event.getOrganizer().getUsername().equals(organizerUsername)) {
            throw new AccessDeniedException("You do not have permission to access this event's analytics");
        }

        long totalRegistered = event.getEventAttendees().size();
        long totalCheckedIn = attendanceRecordRepository.countDistinctAttendeesByEventId(eventId);

        double attendanceRate = (totalRegistered > 0) ? ((double) totalCheckedIn / totalRegistered) * 100.0 : 0.0;

        Map<LocalDate, Long> checkInsByDate = attendanceRecordRepository.countCheckInsByDate(eventId).stream()
                .collect(Collectors.toMap(
                        record -> (LocalDate) record[0],
                        record -> (Long) record[1]
                ));

        return EventAnalyticsResponse.builder()
                .eventId(eventId)
                .eventName(event.getEventName())
                .totalRegistered(totalRegistered)
                .totalCheckedIn(totalCheckedIn)
                .attendanceRate(attendanceRate)
                .checkInsByDate(checkInsByDate)
                .build();
    }
}
