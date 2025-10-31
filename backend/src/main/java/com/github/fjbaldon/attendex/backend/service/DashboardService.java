package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.DailyActivityDto;
import com.github.fjbaldon.attendex.backend.dto.DashboardDto;
import com.github.fjbaldon.attendex.backend.dto.DashboardStatsDto;
import com.github.fjbaldon.attendex.backend.model.Event;
import com.github.fjbaldon.attendex.backend.repository.AttendanceRecordRepository;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import com.github.fjbaldon.attendex.backend.repository.EventRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;
    private final ScannerRepository scannerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @Transactional(readOnly = true)
    public DashboardDto getFullDashboardData(Long organizationId) {
        DashboardStatsDto stats = getOrganizationStats(organizationId);
        List<DashboardDto.UpcomingEventDto> upcomingEvents = getUpcomingEvents(organizationId, 5);
        List<DashboardDto.RecentEventStatsDto> recentEvents = getRecentEventStats(organizationId, 5);

        return DashboardDto.builder()
                .stats(stats)
                .upcomingEvents(upcomingEvents)
                .recentEventStats(recentEvents)
                .build();
    }

    private List<DashboardDto.UpcomingEventDto> getUpcomingEvents(Long organizationId, int limit) {
        return eventRepository.findByOrganizationIdAndStartDateAfterOrderByStartDateAsc(
                organizationId, Instant.now(), PageRequest.of(0, limit)
        ).stream().map(this::toUpcomingEventDto).collect(Collectors.toList());
    }

    private List<DashboardDto.RecentEventStatsDto> getRecentEventStats(Long organizationId, int limit) {
        return eventRepository.findByOrganizationIdAndEndDateBeforeOrderByEndDateDesc(
                organizationId, Instant.now(), PageRequest.of(0, limit)
        ).stream().map(this::toRecentEventStatsDto).collect(Collectors.toList());
    }


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

    private DashboardDto.UpcomingEventDto toUpcomingEventDto(Event event) {
        return DashboardDto.UpcomingEventDto.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .startDate(event.getStartDate())
                .build();
    }

    private DashboardDto.RecentEventStatsDto toRecentEventStatsDto(Event event) {
        long registered = event.getEventAttendees().size();
        long checkedIn = attendanceRecordRepository.countDistinctAttendeesByEventId(event.getId());
        return DashboardDto.RecentEventStatsDto.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .totalRegistered(registered)
                .totalCheckedIn(checkedIn)
                .build();
    }
}
