package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.AnalyticsBreakdownDto;
import com.github.fjbaldon.attendex.backend.model.Event;
import com.github.fjbaldon.attendex.backend.repository.AttendanceRecordRepository;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final AttendeeRepository attendeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<String> getAvailableCustomFields(Long organizationId) {
        return attendeeRepository.findDistinctCustomFieldKeysByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public AnalyticsBreakdownDto getCustomFieldBreakdown(Long eventId, String customFieldKey, Long organizationId) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Event not found in your organization."));

        List<AnalyticsBreakdownDto.BreakdownItem> breakdownItems = attendanceRecordRepository.countAttendeesByCustomField(event.getId(), customFieldKey);
        long totalCheckedIn = attendanceRecordRepository.countDistinctAttendeesByEventId(event.getId());

        return AnalyticsBreakdownDto.builder()
                .breakdown(breakdownItems)
                .totalCheckedIn(totalCheckedIn)
                .build();
    }
}
