package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.DailyActivityDto;
import com.github.fjbaldon.attendex.backend.dto.DashboardStatsDto;
import com.github.fjbaldon.attendex.backend.repository.AttendanceRecordRepository;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;
    private final ScannerRepository scannerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getOrganizationStats(Long organizationId) {
        long totalEvents = eventRepository.countByOrganizationId(organizationId);
        long totalAttendees = attendeeRepository.countByOrganizationId(organizationId);
        long totalScanners = scannerRepository.countByOrganizationId(organizationId);

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long liveCheckIns = attendanceRecordRepository.countByOrganizationIdAndCheckInTimestampAfter(organizationId, oneHourAgo);

        return DashboardStatsDto.builder()
                .totalEvents(totalEvents)
                .totalAttendees(totalAttendees)
                .totalScanners(totalScanners)
                .liveCheckIns(liveCheckIns)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DailyActivityDto> getActivityOverTime(Long organizationId, Instant startDate) {
        return attendanceRecordRepository.findDailyActivitySince(organizationId, startDate);
    }
}
