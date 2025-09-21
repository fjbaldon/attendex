package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.ActiveEventResponse;
import com.github.fjbaldon.attendex.backend.dto.AttendanceSyncRequest;
import com.github.fjbaldon.attendex.backend.dto.EventAttendeeSyncResponse;
import com.github.fjbaldon.attendex.backend.mapper.AppMapper;
import com.github.fjbaldon.attendex.backend.model.AttendanceRecord;
import com.github.fjbaldon.attendex.backend.model.Event;
import com.github.fjbaldon.attendex.backend.model.Scanner;
import com.github.fjbaldon.attendex.backend.repository.AttendanceRecordRepository;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppService {

    private final EventRepository eventRepository;
    private final ScannerRepository scannerRepository;
    private final AttendeeRepository attendeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AppMapper appMapper;

    @Transactional(readOnly = true)
    public List<ActiveEventResponse> getActiveEvents(String scannerUsername) {
        Scanner scanner = findScannerByUsername(scannerUsername);
        LocalDate today = LocalDate.now();

        return eventRepository.findAllByOrganizerAndDate(scanner.getOrganizer(), today).stream()
                .map(appMapper::toActiveEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventAttendeeSyncResponse> getAttendeesForEvent(Long eventId, String scannerUsername) {
        Scanner scanner = findScannerByUsername(scannerUsername);
        Event event = findEventById(eventId);

        if (!event.getOrganizer().getId().equals(scanner.getOrganizer().getId())) {
            throw new AccessDeniedException("You do not have permission to access this event's attendee list");
        }

        return event.getEventAttendees().stream()
                .map(appMapper::toEventAttendeeSyncResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void syncAttendance(AttendanceSyncRequest request, String scannerUsername) {
        Scanner scanner = findScannerByUsername(scannerUsername);

        List<AttendanceRecord> recordsToSave = request.getRecords().stream().map(recordDto -> {
            Event event = findEventById(recordDto.getEventId());

            if (!event.getOrganizer().getId().equals(scanner.getOrganizer().getId())) {
                throw new AccessDeniedException("Access denied for event ID: " + event.getId());
            }

            return AttendanceRecord.builder()
                    .event(event)
                    .attendee(attendeeRepository.getReferenceById(recordDto.getAttendeeId()))
                    .scanner(scanner)
                    .checkInTimestamp(recordDto.getCheckInTimestamp())
                    .build();
        }).collect(Collectors.toList());

        attendanceRecordRepository.saveAll(recordsToSave);
    }

    private Scanner findScannerByUsername(String username) {
        return scannerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Scanner not found"));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with ID " + eventId + " not found"));
    }
}
